# SP-01: MQTT broker para ingesta de sensores IoT

> Los datos de planes gratuitos cambian con frecuencia y aquí vienen de conocimiento general, no de una consulta
> en vivo. Confirmar en las páginas de precios antes de decidir.

## Comparación

| Criterio | Mosquitto | EMQX | HiveMQ |
|---|---|---|---|
| Compatibilidad con Spring Boot | Cualquier cliente MQTT estándar (Paho, Spring Integration MQTT). El broker es independiente de la app. | Igual | Igual. Además tiene su propio cliente (`hivemq-mqtt-client`), opcional |
| Plan gratuito | Totalmente gratis (open source, EPL/EDL). Se autoaloja | Edición open source autoalojada gratis; EMQX Cloud tiene plan Serverless con cuota gratuita mensual | HiveMQ Cloud Serverless con plan gratuito (límite de conexiones y tráfico); Community Edition open source autoalojada |
| Facilidad de integración | Muy alta en local: una imagen Docker y 2 líneas de config. En producción hay que operarlo uno mismo | Alta: dashboard web, reglas y webhooks (podría llamar al endpoint REST sin código) | Más alta si se usa Cloud: sin servidores que mantener, cuenta y credenciales en minutos |
| TLS | Sí (listener 8883 con certificados propios) | Sí (propio o gestionado en Cloud) | Sí, obligatorio en Cloud (8883, CA pública) |
| Autenticación | Archivo de usuarios/contraseñas, ACL por tópico, plugins | Usuario/contraseña, JWT, ACL, base de datos externa | Usuario/contraseña por credencial en Cloud, ACL por rol |
| Operación | Tú lo mantienes | Cloud: gestionado. Self-hosted: tú | Cloud: gestionado |

## Recomendación

1. **Desarrollo y demo: Mosquitto** en Docker (`scripts/mqtt/docker-compose.mosquitto.yml`). Cero cuentas, cero costo.
2. **Entorno compartido o producción: HiveMQ Cloud (plan gratuito)** o EMQX Cloud Serverless. Dan TLS y
   autenticación sin operar un broker. Como el cliente es Paho estándar, cambiar de broker es solo cambiar
   `MQTT_BROKER_URL`, `MQTT_USERNAME` y `MQTT_PASSWORD`.
3. Solo si hay que operar el broker uno mismo (más de unos cientos de sensores, o requisitos de datos on-premise),
   considerar Mosquitto o EMQX autoalojado con TLS y ACL.

No hay lock-in: ninguno de los tres exige cambios en el código del backend.

## Seguridad

- **TLS**: usar `ssl://host:8883`. Con brokers cloud la CA es pública y la JVM ya confía en ella. Con una CA propia
  (Mosquitto autoalojado) hay que dar a Paho un `SocketFactory` con el truststore.
- **Autenticación del broker**: usuario/contraseña por cliente. El backend usa uno con permiso solo de **lectura**
  sobre `sensors/+/readings`. Cada dispositivo debería tener el suyo con permiso de **escritura** solo sobre
  `sensors/{su-id}/readings` (ACL).
- **Autenticación de la API**: el endpoint exige `X-Device-Key`. El puente la toma del campo `deviceKey` del mensaje
  o, si no viene, de `MQTT_DEFAULT_DEVICE_KEY` (solo para la demo).
- Pendiente de decidir: el `deviceKey` viaja dentro del payload MQTT. Con TLS está cifrado en tránsito, pero cualquier
  suscriptor del tópico lo vería, por eso la ACL debe impedir que otros lean esos tópicos.

## Prototipo

Código en `telemetry/infrastructure/mqtt/`. Es un cliente Paho que se suscribe a `sensors/+/readings` y reenvía cada
mensaje a `POST /api/v1/sensors/readings` por HTTP. Así se reutiliza toda la validación, umbrales y alertas.
Está **desactivado por defecto** (`MQTT_ENABLED=false`).

- Tópico: `sensors/{sensorId}/readings`
- Payload: `{"value": 71.5, "unit": "C", "measuredAt": "2026-11-04T12:00:00Z", "sourceKey": "opcional", "deviceKey": "opcional"}`
- Si falta `sourceKey` se genera `mqtt-{sensorId}-{measuredAt}`, así un reenvío del mismo mensaje no duplica la lectura.
- Un mensaje inválido o rechazado por la API (4xx) se registra y se descarta; nunca tumba la conexión.
- Suscripción persistente (`cleanSession=false`, QoS 1) y reconexión automática.

### Probarlo

```bash
docker compose -f scripts/mqtt/docker-compose.mosquitto.yml up -d
MQTT_ENABLED=true MQTT_DEFAULT_DEVICE_KEY=<clave-del-sensor> ./mvnw spring-boot:run
mosquitto_pub -h localhost -t "sensors/<uuid-del-sensor>/readings" \
  -m '{"value":71.5,"unit":"C","measuredAt":"2026-11-04T12:00:00Z"}'
```

Los logs deben mostrar `reading of sensor ... forwarded -> HTTP 201`.

### Límites del prototipo (fuera de alcance)

- Si el broker está caído al arrancar no reintenta la primera conexión (la reconexión automática de Paho solo actúa
  tras haber conectado una vez). Resolver con un reintento programado antes de producción.
- Pasar por HTTP añade un salto de red a sí mismo; si el volumen crece, llamar directamente a `ReadingCommandService`.
- Sin tests automáticos: requeriría un broker embebido o Testcontainers.
