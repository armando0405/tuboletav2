# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Backend de TuBoleta v2 — Spring Boot 4.0.5 REST API (Java 21, Maven). Parte del monorepo `tuboletav2`; ver el `CLAUDE.md` de la raíz para la visión completa (dominio, frontend, docker) y la carpeta `../requerimientos/` para los requerimientos formales (los REQ-ARQ-* definen la arquitectura de este backend).

## Comandos

```bash
mvnw.cmd clean install                        # build (Windows; en Unix ./mvnw)
mvnw.cmd spring-boot:run                      # correr (http://localhost:8088)
mvnw.cmd test                                 # todos los tests
mvnw.cmd test -Dtest=ClassName#methodName     # un solo test
```

- Swagger UI: `http://localhost:8088/swagger` · OpenAPI JSON: `/api-docs`
- Requiere Postgres corriendo: `docker-compose up -d` desde la raíz del repo.

## Arquitectura (REQ-ARQ-001)

Capas clásicas bajo `com.tuboleta.backend` — hexagonal fue descartada explícitamente:

```
api/controllers  → REST; delegan SIEMPRE en una interfaz de servicio, sin lógica de negocio
api/dtos         → request bodies como RECORDS de Java (inmutables, con Bean Validation) (REQ-ARQ-003)
service          → interfaces de servicio
service/impl     → implementaciones (única capa que inyecta repositorios)
repository       → Spring Data JPA
domain/entities  → entidades JPA (clases, no records; mapeos consistentes con las migraciones Flyway)
config           → configuración (security, persistence)
utils            → TODO lo reutilizable vive aquí (REQ-ARQ-002)
```

Separación por dominio (REQ-ARQ-004): scraping, detección de cambios, notificaciones y envío por canal son servicios distintos; los schedulers **solo orquestan** (seleccionan trabajo pendiente e invocan servicios). El envío por canal va detrás de una interfaz: agregar Telegram/WhatsApp = nueva implementación, sin tocar el flujo.

### Mapa de lo implementado (todo construido y con tests)

- **Controllers** (`api/controllers`): `Auth` (register/login/logout/me), `Search` (CRUD + `/{id}/events` + `/{id}/toggle` pausa total + `/{id}/providers/{pid}/toggle` pausa por par), `Provider` (`GET /api/providers`, fuentes ACTIVE para el select de búsquedas), `Destination`, `Notification` (inbox + unread-count + read/read-all), `AdminProvider` (`/api/admin/**`, disable/enable con notificación).
- **Seguridad** (`config/security`): sesión (cookie) + `BCrypt` + CORS a `:7075` + `AdminBootstrapRunner` (crea el ADMIN inicial si `users` está vacía). 401/403 devuelven el envelope JSON, nunca la página de login.
- **Scheduler** (`service/scheduler`): `MonitoringDispatcher` (`@Scheduled`), `DueWorkSelector`, `MonitoringRunService` (orquesta, no transaccional), `MonitoringPersistenceService` (parte transaccional). Explicado de punta a punta en **`../requerimientos/artefactos/flujo-scheduler.md`** (léelo antes de tocar el monitoreo).
- **Detección** (`service/impl/ChangeDetectionServiceImpl`) + **extracción** (`service/extraction/TuBoletaScraperExtractor`, Jsoup; la URL sale de `providers.search_url` con `{term}` URL-encoded) + **notificaciones** (`service/impl/NotificationServiceImpl` + senders `SendGrid`/`Logging` bajo `ChannelSender`).
- **Logs**: el scheduler y el envío de correo loguean a INFO (cuándo corre, qué extrae, cambios detectados, resultado de cada corrida y de cada envío de email).

## Convenciones obligatorias

- Todo endpoint devuelve `ObjectResponse<T>` (objeto) u `ObjectListResponse<T>` (lista) de `utils/response`: `code` 0 = éxito / -1 = error + `msg`. Nunca un DTO pelado — el interceptor de axios del frontend depende de este contrato.
- Errores: lanzar `BusinessException` / `GenericException` / `NotFoundRegisterException` (`utils/exception`); el `GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza las respuestas. Excepciones nuevas se registran ahí. Mensajes en `ErrorMessage` (español, formateados con `MessageFormat`).
- Nombres de paquetes/clases en inglés; comentarios, logs y mensajes al usuario en español.
- Secretos jamás en archivos versionados — variables de entorno (REQ-NOT-005; lección de v1).

## Base de datos

Flyway es el único dueño del esquema (`ddl-auto: validate` — nunca volver a `update`). Migraciones en `src/main/resources/db/migration/`: `V1__init.sql` (esquema + seed) y `V2__fix_tuboleta_search_url.sql` (corrige la URL del proveedor). Regla práctica: si la BD **ya se aplicó y tiene datos que quieres conservar**, NO edites V1 (romperías el checksum de Flyway); agrega una migración `V{n}` nueva (así se hizo con V2). Editar V1 solo es válido pre-despliegue y obliga a recrear la BD (`docker-compose down -v`). Detalle completo de tablas: `CLAUDE.md` de la raíz y `../requerimientos/artefactos/esquema-bd.md`.

Secretos (ej. `SENDGRID_API_KEY`, `EMAIL_FROM`): por variable de entorno, **nunca** como valor real en `application.yaml` versionado (REQ-NOT-005). El envío por email usa `SendGridEmailSender` si hay API key, o `LoggingEmailSender` (simula el envío) si no la hay.
