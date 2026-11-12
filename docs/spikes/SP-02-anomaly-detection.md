# SP-02: Detección estadística de anomalías

> Tribuo y Smile se evaluaron por su documentación y conocimiento general; **no se probaron en el proyecto**.
> Solo Commons Math se integró y ejecutó. Las licencias deben verificarse en los repositorios antes de decidir.

## Contexto

Hoy una lectura es anómala solo si sale del rango `[mínimo, máximo]` configurado. Una temperatura que siempre
está en 70 ± 1 y de pronto marca 78 sigue "en rango" si el máximo es 100, aunque sea claramente un comportamiento raro.
La idea: comparar la lectura con el comportamiento reciente **de ese mismo sensor**.

## Comparación

| Criterio | Apache Commons Math | Tribuo (Oracle) | Smile |
|---|---|---|---|
| Facilidad de integración | Muy alta: una dependencia, sin transitivas, `DescriptiveStatistics` hace media/desviación sobre ventana | Media: API propia (Dataset, Trainer, Model, Prediction), más conceptos y dependencias | Media: API amplia, incluye detección de anomalías (p. ej. isolation forest) pero más pesada |
| Licencia | Apache 2.0 | Apache 2.0 | **Verificar**: las versiones recientes se publican bajo GPL-3.0 (con licencia comercial aparte), lo que puede obligar a liberar el backend con la misma licencia |
| Spring Boot | Es una librería Java pura, se usa como cualquier bean | Igual | Igual |
| Servicio externo | No | No | No |
| Mantenimiento | `math3` 3.6.1 es la última versión (2016), estable pero sin novedades. El sucesor es `commons-statistics` | Activo | Activo |
| Capacidad | z-score, media móvil, percentiles, regresión simple | Modelos entrenados (clustering, one-class SVM, etc.) | Igual de amplia, con isolation forest y más |

## Recomendación

**Apache Commons Math para esta etapa.** El requisito (z-score sobre las últimas N lecturas) es estadística
descriptiva, no aprendizaje automático. Es la opción más simple, con licencia sin restricciones y sin riesgo de
sobreingeniería. Tribuo o Smile solo se justifican si más adelante se necesitan modelos multivariable o
estacionales (por ejemplo isolation forest sobre varios sensores de un activo). Si se considera Smile, resolver antes
el tema de la licencia GPL.

## Prototipo

```
ReadingCommandServiceImpl.handle()
   ├─ lee las últimas N lecturas del sensor (ANTES de guardar la actual)
   ├─ guarda la lectura y evalúa el umbral (flujo existente, sin cambios)
   └─ checkStatistically(): z = (x − media) / desviación; si |z| > umbral, log WARN
```

| Pieza | Archivo |
|---|---|
| Puerto | `telemetry/domain/services/StatisticalAnomalyDetector`, `StatisticalVerdict` |
| Implementación | `telemetry/infrastructure/statistics/ZScoreAnomalyDetector` |
| Consulta de historial | `SensorReadingRepository.findRecentBySensorId` |
| Integración | `ReadingCommandServiceImpl` |
| Tests | `ZScoreAnomalyDetectorTest` (5 casos) |

Configuración (desactivado por defecto):

| Variable | Defecto | Significado |
|---|---|---|
| `TELEMETRY_STATISTICAL_ENABLED` | `false` | Activa la detección |
| `TELEMETRY_STATISTICAL_WINDOW` | `50` | Últimas N lecturas |
| `TELEMETRY_STATISTICAL_MIN_SAMPLES` | `10` | Mínimo de historial; con menos no se evalúa |
| `TELEMETRY_STATISTICAL_Z` | `3.0` | Umbral de |z| |

Decisiones de diseño:
- **Modo sombra**: solo escribe un WARN en el log. No crea `AnomalyDetection` ni alertas, porque ese agregado exige una
  `ThresholdRule` y su tabla no tiene dónde guardar un z-score. Así no cambia ningún comportamiento actual ni el
  esquema de la base de datos.
- El historial se lee **antes** de guardar, para que la lectura no se compare consigo misma.
- Con historial insuficiente no se evalúa (evita falsas alarmas al inicio).
- Si la señal es constante (desviación 0), cualquier cambio se marca como anómalo en lugar de dividir entre cero.
- Si la lectura es un duplicado (mismo `sourceKey`) o la rechazan las validaciones, el detector no se ejecuta.
- Los tests del detector pasan. **No hay un test de integración de la ingesta con el detector activo.**

## Limitaciones y siguientes pasos

1. **Persistir el resultado y generar alertas**: requiere migrar `AnomalyDetection` (regla opcional, tipo de
   detección, z-score) y decidir la severidad. Es la principal decisión pendiente.
2. **El z-score asume una distribución aproximadamente normal y estable.** Una tendencia (desgaste progresivo, que es
   justo el caso de mantenimiento predictivo) infla la desviación y puede ocultar anomalías o, al revés, marcar todo
   tras un cambio de régimen legítimo. Alternativas: media móvil exponencial, o z-score robusto con mediana y MAD.
3. **Un valor anómalo entra al historial** y contamina las siguientes ventanas. Considerar excluir los marcados.
4. **Costo**: una consulta extra de N filas por lectura. Con la ventana de 50 es aceptable; con mucho volumen, mantener
   la ventana en memoria o en caché por sensor.
5. Los parámetros (N = 50, z = 3) son valores iniciales; calibrarlos con datos reales del simulador (`scripts/demo`).

## Cómo probarlo

```bash
TELEMETRY_STATISTICAL_ENABLED=true ./mvnw spring-boot:run
python3 scripts/demo/seed_demo.py                # una vez
python3 scripts/demo/simulator.py run            # deja pasar más de 10 lecturas por sensor
# luego enviar una lectura atípica pero DENTRO del rango del umbral (con la API o Swagger);
# `simulator.py spike` no sirve para esto porque supera el límite y dispara el umbral normal.
# buscar en el log: "SP-02 statistical anomaly"
```

Esta prueba de extremo a extremo no se ejecutó; solo se ejecutaron los tests unitarios del detector.
