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

## Convenciones obligatorias

- Todo endpoint devuelve `ObjectResponse<T>` (objeto) u `ObjectListResponse<T>` (lista) de `utils/response`: `code` 0 = éxito / -1 = error + `msg`. Nunca un DTO pelado — el interceptor de axios del frontend depende de este contrato.
- Errores: lanzar `BusinessException` / `GenericException` / `NotFoundRegisterException` (`utils/exception`); el `GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza las respuestas. Excepciones nuevas se registran ahí. Mensajes en `ErrorMessage` (español, formateados con `MessageFormat`).
- Nombres de paquetes/clases en inglés; comentarios, logs y mensajes al usuario en español.
- Secretos jamás en archivos versionados — variables de entorno (REQ-NOT-005; lección de v1).

## Base de datos

Flyway es el único dueño del esquema (`ddl-auto: validate` — nunca volver a `update`). Migraciones en `src/main/resources/db/migration/`. `V1__init.sql` es editable solo mientras no haya despliegue compartido (editar = recrear BD local con `docker-compose down -v`); después, todo cambio es una migración nueva. Detalle completo de tablas: `CLAUDE.md` de la raíz y `../requerimientos/artefactos/esquema-bd.md`.
