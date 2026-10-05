# PredictiveMaintain — Backend

API REST de **PredictiveMaintain**, una plataforma de auditoría y mantenimiento predictivo para plantas industriales con sensores IoT. Proyecto del curso **1ACC0238 Aplicaciones para Dispositivos Móviles** (UPC), equipo **PredictiveTech**.

- **API en producción:** `https://TU-URL.up.railway.app`
- **Documentación (Swagger):** `https://TU-URL.up.railway.app/swagger-ui.html`

## Qué hace

Una empresa registra sus equipos, instala sensores y define el rango permitido de cada variable. Cuando una lectura sale del rango, el sistema crea una alerta con los datos que la originaron. El jefe de mantenimiento la confirma, se genera una orden de trabajo, un técnico la atiende con evidencia fotográfica y la alerta se resuelve. Además hay reportes de disponibilidad y OEE, estimación de vida útil restante y un plan de suscripción por cantidad de activos.

## Arquitectura

Monolito modular con **cuatro bounded contexts**, cada uno con las capas `domain / application / infrastructure / interfaces` (Domain-Driven Design):

| Contexto | Responsabilidad |
|---|---|
| `iam` | Cuentas, roles (jefe, técnico, operador), login con JWT, recuperación de contraseña |
| `subscription` | Empresas, planes, cupo de activos, facturas internas |
| `maintenance` | Activos, alertas, órdenes de trabajo, evidencias, reportes |
| `telemetry` | Sensores, lecturas, umbrales, detección de anomalías, vida útil restante |

Los contextos solo se hablan a través de fachadas (`interfaces/acl`), nunca accediendo a las tablas de otro.

## Tecnologías

Java 21 · Spring Boot 3.4 · Spring Security + JWT · Spring Data JPA · PostgreSQL 16 · springdoc-openapi · Maven · Docker · GitHub Actions · JUnit 5 + Testcontainers

## Cómo ejecutarlo

### Con Docker (igual que producción)
```bash
cp .env.example .env        # completa DATABASE_PASSWORD y JWT_SECRET
docker compose up --build
```
La API queda en http://localhost:8080 y Swagger en http://localhost:8080/swagger-ui.html.

### En desarrollo (IntelliJ)
```bash
docker run --name pm-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=predictivemaintain -p 5432:5432 -d postgres:16
./mvnw spring-boot:run
```
Usa el perfil `dev` por defecto.

## Variables de entorno (perfil `prod`)

| Variable | Obligatoria | Qué es |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Sí | Debe valer `prod` |
| `DATABASE_HOST`, `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD` | Sí | Conexión a PostgreSQL |
| `DATABASE_PORT` | No (5432) | Puerto de PostgreSQL |
| `DATABASE_PARAMS` | No | Parámetros de la URL, por ejemplo `?sslmode=require` |
| `JWT_SECRET` | Sí | Secreto de firma de los tokens, mínimo 32 caracteres aleatorios |
| `CORS_ALLOWED_ORIGINS` | No (`*`) | Orígenes web autorizados |
| `EVIDENCE_STORAGE_DIR` | No (`./data/evidence`) | Carpeta de las fotos de evidencia (debe ser un volumen persistente) |
| `PASSWORD_RESET_LOG_LINKS` | No (`false`) | `true` escribe el enlace de recuperación en el log (solo demos) |
| `PORT` | No (8080) | Lo define la plataforma |

Si falta una variable obligatoria, la aplicación **no arranca**.

## Pruebas

```bash
./mvnw test        # 93 unitarias + 5 de integración (requiere Docker para estas últimas)
scripts/smoke-test.sh http://localhost:8080     # recorre el flujo principal contra una API en marcha
```
Los criterios de aceptación del informe (formato Gherkin) están cubiertos por pruebas cuyo `@DisplayName` cita el escenario. Cada pull request ejecuta todo en GitHub Actions.

## Endpoints

46 endpoints bajo `/api/v1`, versionados y documentados en Swagger:

| Contexto | Endpoints | Ejemplos |
|---|---|---|
| IAM | 9 | `POST /auth/register`, `POST /auth/login`, `GET /users/me` |
| Subscription | 5 | `GET /plans`, `GET /subscription`, `PUT /subscription/plan`, `GET /invoices` |
| Maintenance | 25 | `/assets`, `/alerts`, `/work-orders`, `/reports/availability`, `/reports/oee` |
| Telemetry | 7 | `POST /sensors/readings`, `GET /assets/{id}/sensors`, `GET /assets/{id}/rul` |

Los errores siguen el formato RFC 7807 y se devuelven en inglés o español según `Accept-Language`.

## Despliegue

Se despliega desde la rama `main` en Railway, con su base PostgreSQL y un volumen para las evidencias. Cada push a `main` redespliega.

## Equipo

PredictiveTech — NRC 4948. *(Completar con los integrantes.)*