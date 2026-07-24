# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TuBoleta v2 is a monorepo with two independent projects:

- `tuboleta-backend/` — Spring Boot 4.0.5 (Java 21) REST API
- `tuboleta-frontend/` — Vue 3 + TypeScript admin SPA (Vite, Vuetify)

The domain is an event-tracking/notification service: users configure "searches" (a term monitored on one or MORE providers like TuBoleta.com — N:M via `search_providers`), a scheduler polls each provider at a user-chosen frequency, diffs results against the immutable `events` history, and notifies (email via SendGrid + in-app inbox) when events are new, changed, or removed.

See `ESTRUCTURA_PROYECTO.md` at the repo root for the full directory tree of both projects — refer to it instead of re-deriving folder structure from scratch.

Each subproject has its own `CLAUDE.md` (`tuboleta-backend/CLAUDE.md`, `tuboleta-frontend/CLAUDE.md`) with project-specific architecture and conventions. Formal requirements live in `requerimientos/` (index: `requerimientos/00-INDICE.md`) — architecture and schema decisions are anchored to REQ files there.

## Commands

### Backend (`tuboleta-backend/`)

```bash
./mvnw clean install       # build
./mvnw spring-boot:run     # run (http://localhost:8088)
./mvnw test                # run all tests
./mvnw test -Dtest=ClassName#methodName   # run a single test
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

- Swagger UI: `http://localhost:8088/swagger`, OpenAPI JSON: `/api-docs`
- Requires Postgres running (see Database section below)

### Frontend (`tuboleta-frontend/`)

```bash
npm run dev         # dev server, http://localhost:7075
npm run build       # vue-tsc --noEmit && vite build
npm run typecheck   # vue-tsc --noEmit only
npm run lint        # eslint . --fix
npm run preview     # preview production build, port 5050
```

There is no frontend test script configured.

### Database / local infra (repo root)

```bash
docker-compose up -d   # Postgres 16 + pgAdmin
docker-compose down
```

Copy the root `env` file to `.env` to override defaults (DB name/user/password, pgAdmin credentials/port). Flyway migrations run automatically against Postgres on backend startup.

Note: pgAdmin's default port (`5050`) and the frontend's `npm run preview` port (`5050`) collide if both are run with defaults — change one if you need both simultaneously.

## Backend Architecture

Base package: `com.tuboleta.backend`. The full layered app is implemented (REQ-ARQ-001): `api/controllers` + `api/dtos` (request/response **records**) → `service` (interfaces) + `service/impl` → `repository` (Spring Data JPA); `domain/entities` + `domain/enums`; `config/security` (session auth + BCrypt + CORS + bootstrap admin). The domain services are split by responsibility (REQ-ARQ-004): `service/extraction` (Jsoup scraper for TuBoleta), `service/detection` + `ChangeDetectionService` (NEW/CHANGED/REMOVED diff), `service/notification` (SendGrid + logging senders behind `ChannelSender`) + `NotificationService` (inbox + fan-out), and `service/scheduler` (the monitoring dispatcher — orchestration only). The controllers implemented: `Auth`, `Search`, `Provider` (list active), `Destination`, `Notification`, `AdminProvider`. Full HTTP contract + entities in `tuboleta-backend/CLAUDE.md`; the scheduler is explained end-to-end in **`requerimientos/artefactos/flujo-scheduler.md`**.

The reusable core lives under `utils/`:

- `constants/ErrorCode.java`, `constants/ErrorMessage.java` — numeric response codes (`SUCCESS = 0`, `ERROR = -1`) and Spanish, `MessageFormat`-parameterized error messages (`{0}`, `{1}`, ...)
- `exception/` — `BusinessException`, `GenericException` (carries an HTTP status), `NotFoundRegisterException`, all with a `messageKey` + `args` for i18n-style formatting
- `exception/GlobalExceptionHandler.java` — `@RestControllerAdvice` that centralizes all error handling; catches the three custom exceptions plus Spring's validation/routing exceptions and a catch-all `Exception`. New exception types should be added here.
- `response/ObjectResponse<T>` and `response/ObjectListResponse<T>` — the standard REST envelope (`code`, `msg`, `object`/`list`). Every controller endpoint should return one of these rather than a raw body or bare DTO.

Conventions to follow when adding backend code:
- New endpoints return `ObjectResponse<T>` (single) or `ObjectListResponse<T>` (collection)
- New failure cases should throw one of the existing exception types (or a new one wired into `GlobalExceptionHandler`) rather than handling errors ad hoc in controllers
- Error message strings live in `ErrorMessage`, keyed by constant, and are formatted with `MessageFormat`
- Package/class names are English; log/user-facing messages and code comments are Spanish, matching the existing style

### Database

Postgres 16 (Docker), managed with Flyway. Migrations live in `tuboleta-backend/src/main/resources/db/migration/`, named `V{n}__description.sql`.

**Editability rule (REQ-BD-003):** `V1__init.sql` is editable while no shared deployment exists; any edit requires recreating the local DB (`docker-compose down -v`). Once a shared deployment exists, V1 is frozen and every change goes in a new `V{n}__...` migration. The **conceptual** definition of the schema lives in `requerimientos/` (one REQ per decision; table→REQ map in `requerimientos/artefactos/esquema-bd.md`) — schema edits must be anchored to a confirmed REQ, never to loose conversation.

Key tables from `V1__init.sql` and their intent:
- `providers` — source catalog; `provider_type` (`SCRAPER`/`API`) + `config` (JSONB) hold per-provider extraction config; `status` `ACTIVE`/`DISABLED` + `status_reason` (REQ-FUE-002): disabling skips its searches at runtime, notifies affected users (`PROVIDER_DISABLED`), and is ADMIN-only
- `notif_channels` — EMAIL is active; TELEGRAM/WHATSAPP rows exist but are inactive (deliberate technical debt for future channels)
- `users` — `role` `ADMIN`/`USER` in a single table; `status` `INACTIVE` blocks login and pauses the user's searches
- `user_notification_channels` — per-user reusable notification destinations (a user can have several emails)
- `searches` — user term; stores `term` as typed plus `term_normalized` (unique per user via partial index excluding `DELETED`); `check_frequency_hours` (6/12/24/48) is user-chosen; `status` `ACTIVE`/`INACTIVE`/`DELETED`, deletion always logical
- `search_providers` — N:M search↔provider; each pair pauses independently and tracks its own `last_run_at`
- `provider_runs` — scheduler run log (REQ-DET-005): one row per real HTTP request (pairs are grouped by provider+normalized term); `success` distinguishes clean runs from failed/partial ones — only fully successful runs count toward `miss_count`/REMOVED
- `search_notifications` — fan-out: which of the user's destinations get alerts for a search
- `events` — immutable per-pair event history keyed by `external_id` (no hash — change detection is field-by-field diff, REQ-DET-001); `miss_count` counts consecutive successful runs without seeing the event (REMOVED at 2, never on a failed run — REQ-DET-003); `raw_json` keeps the full source payload
- `event_changes` — one row per changed field per event, for audit/diffing
- `notifications` — the notifiable fact and in-app inbox (`read_at` = read state); feeds the "new/changed" highlight in the dashboard
- `notifications_log` — per-channel delivery audit (FK to `notifications`, destination stored as text snapshot)

`spring.jpa.hibernate.ddl-auto` is `validate` — Flyway is the only owner of the schema; Hibernate just checks that entity mappings match the migrations. Never switch it back to `update`.

## Frontend Architecture

Stack: Vue 3.5 + TypeScript, Vuetify 4 (Material Design), Vite, Pinia, Vue Router 5, axios, SweetAlert2 for modals/alerts. No vue-i18n — Spanish-only project, visible text goes directly in components. Path alias `@` → `src/`.

The "Dark Operations" theme (dark, indigo accent, Inter + JetBrains Mono — `requerimientos/artefactos/diseno-frontend.md`) is applied and all self-service screens are built (REQ-FE-001..005): a summary **dashboard** (`/home`), **búsquedas** (list + create/edit + pause whole-search or per-provider + logical delete), **eventos por búsqueda**, **notificaciones** (inbox, explicit mark-read), **destinos**, and **admin de fuentes** (ADMIN only). The old-project features were removed first (REQ-ARQ-005); the kept infra is Login, layouts (blank/full), plugins, stores (auth/notify), and reusable components (`TableDynamic`, `Loading`, `FloatingCard`, `AppSnackbarQueue`).

**Request flow:** view → composable (`use*`, module-level shared state) → service (typed wrapper) → shared axios instance → backend. The screen-building pattern (Header/Body pair + `use*` composable + endpoints/services entries) is documented with a full example in `requerimientos/artefactos/patron-frontend.md` — read it before building a new screen.

- `plugins/axios.ts` — shared axios instance: base URL from `VITE_API_URL` (= `/api`), `withCredentials: true`; the response interceptor treats the backend's `code === -1` envelope as an error and redirects to login on 401 (guarded against double-redirect). **Dev proxy:** `vite.config.ts` proxies `/api` → `http://localhost:8088`, so the SPA and API are same-origin in dev and the session cookie travels without CORS issues.
- `types/services/` — DTOs and the response envelope types (`ObjectResponse<T>`, `ObjectListResponse<T>` — mirrors the backend envelope)
- `stores/auth.store.ts` — current user + `isAuthenticated` + `isAdmin`; `clearUser()` resets all composable state on logout/401; `stores/notify.store.ts` — global snackbar queue via `useNotify()` + `AppSnackbarQueue.vue`

Routing: `MainRoutes` (requires auth, `FullLayout`) + `AuthRoutes` (no auth, `BlankLayout`) composed in `router/index.ts`. The `beforeEach` guard is **active**: unauthenticated → login, and `/admin/**` (`meta.requiresAdmin`) → home if not ADMIN. Active/inactive states use real **booleans** (the old `'S'/'N'` convention was removed).

See also `tuboleta-backend/CLAUDE.md` and `tuboleta-frontend/CLAUDE.md` for per-project detail.

## Conventions

- Commit messages follow `[FAMA] Type: description` in Spanish (e.g. `Feat:`, `Fix:`) — match this format for new commits.
- There are no `.cursorrules`, `.cursor/rules/`, or Copilot instructions files in this repo.
