# Estructura del Proyecto TuBoleta v2

> Refleja el estado REAL del repo. Los paquetes del backend marcados como *(objetivo)* aún no existen: son el layout a seguir al implementar (REQ-ARQ-001).

```
tuboletav2/
│
├── CLAUDE.md                                   # Guía raíz para Claude Code
├── ESTRUCTURA_PROYECTO.md                      # Este archivo
├── docker-compose.yml                          # Postgres 16 + pgAdmin
├── env                                         # Plantilla de variables (copiar a .env)
│
├── requerimientos/                             # Fuente de verdad conceptual del proyecto
│   ├── 00-INDICE.md                             # Tabla maestra de requerimientos
│   ├── bitacora-cambios.md                      # Changelog de decisiones
│   ├── _borrador.md                             # Ideas sin formalizar
│   ├── borrador-requerimientos.md               # Histórico (congelado)
│   ├── version-1-analisis.md                    # Análisis de la v1 (scraper original)
│   ├── arquitectura/    (REQ-ARQ-001..005)
│   ├── base-datos/      (REQ-BD-001..003)
│   ├── busquedas/       (REQ-BUS-001..005)
│   ├── deteccion/       (REQ-DET-001..005)
│   ├── frontend-autoservicio/ (REQ-FE-001..005)
│   ├── fuentes/         (REQ-FUE-001..002)
│   ├── notificaciones/  (REQ-NOT-001..005)
│   ├── usuarios/        (REQ-USU-001..002)
│   └── artefactos/
│       ├── esquema-bd.md                        # Mapa tabla → REQ (enlace a la migración)
│       ├── patron-frontend.md                   # Patrón de construcción de pantallas
│       └── diseno-frontend.md                   # Dirección visual "Dark Operations"
│
├── tuboleta-backend/                           # Spring Boot 4.0.5 (Java 21)
│   ├── CLAUDE.md
│   ├── mvnw / mvnw.cmd / pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/tuboleta/backend/
│       │   │   ├── Main.java                    # @SpringBootApplication
│       │   │   ├── utils/                       # Lo ÚNICO implementado hoy
│       │   │   │   ├── constants/               # ErrorCode, ErrorMessage
│       │   │   │   ├── exception/               # Excepciones + GlobalExceptionHandler
│       │   │   │   └── response/                # ObjectResponse, ObjectListResponse
│       │   │   ├── api/controllers/             # (objetivo) REST controllers
│       │   │   ├── api/dtos/                    # (objetivo) records de entrada
│       │   │   ├── service/ + service/impl/     # (objetivo) interfaces + impl
│       │   │   ├── repository/                  # (objetivo) Spring Data JPA
│       │   │   ├── domain/entities/             # (objetivo) entidades JPA
│       │   │   └── config/                      # (objetivo) security, persistence
│       │   └── resources/
│       │       ├── application.yaml             # puerto 8088, ddl-auto: validate
│       │       └── db/migration/V1__init.sql    # Esquema completo (12 tablas, Flyway)
│       └── test/java/com/tuboleta/backend/MainTests.java
│
└── tuboleta-frontend/                          # Vue 3 + TS + Vuetify 4 (esqueleto post-vaciado)
    ├── CLAUDE.md
    ├── package.json / vite.config.ts / tsconfig.json / eslint.config.mjs
    └── src/
        ├── main.ts / App.vue
        ├── components/
        │   ├── shared/AppSnackbarQueue.vue
        │   └── ui-components/                   # TableDynamic, Loading, FloatingCard
        ├── composables/                         # index.ts, useNotify.ts
        ├── layouts/                             # blank/ y full/ (sidebar, topbar)
        ├── plugins/                             # axios, swal, vuetify (sin i18n)
        ├── router/                              # index, MainRoutes, AuthRoutes, HomeRoutes
        ├── stores/                              # auth.store, notify.store
        ├── theme/LightTheme.ts                  # BlueTheme (será reemplazado por el tema Dark Operations)
        ├── types/                               # Responses, LoginDTO, User, component/
        ├── utils/
        │   ├── endpoints/                       # securityEndpoints + index
        │   └── services/                        # securityServices
        ├── views/
        │   ├── auth/Login.vue
        │   ├── dashboard/Index.vue              # shell con datos demo
        │   └── pages/Error404.vue
        └── scss/
```

---

## Información técnica

### Backend — Spring Boot
- **Framework**: Spring Boot 4.0.5, **Java 21**, Maven (wrapper)
- **Dependencias reales del pom**: Web, Data JPA, Validation, Actuator, Flyway (+ postgres), driver PostgreSQL, Lombok, SpringDoc OpenAPI, Test. *(Spring Security se agregará al implementar REQ-USU-002 — hoy NO está.)*
- Arquitectura en capas y convenciones: REQ-ARQ-001..004 y `tuboleta-backend/CLAUDE.md`

### Frontend — Vue 3
- Vue 3.5 + TypeScript, Vite, **Vuetify 4**, Pinia, Vue Router 5, axios, SweetAlert2, Iconify/MDI
- **Sin vue-i18n** (proyecto monolingüe; textos en español directo)
- Patrón de pantallas: `requerimientos/artefactos/patron-frontend.md`; dirección visual: `diseno-frontend.md`

### Base de datos
- PostgreSQL 16 dockerizado; esquema gestionado SOLO por Flyway (`ddl-auto: validate`)
- 12 tablas — mapa de trazabilidad en `requerimientos/artefactos/esquema-bd.md`

---

**Última actualización**: 4 de julio de 2026 (cierre de la etapa de requerimientos)
