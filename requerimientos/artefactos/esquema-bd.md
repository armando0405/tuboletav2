# Esquema de base de datos — enlace y mapa de trazabilidad

**Fuente ejecutable (única, no se duplica aquí):**
`tuboleta-backend/src/main/resources/db/migration/V1__init.sql`

La definición **conceptual** del esquema son los REQ de esta carpeta; el `.sql` es el artefacto derivado que Flyway aplica automáticamente al arrancar el backend (política en `base-datos/REQ-BD-003-politica-migraciones.md`, incluida la regla de editabilidad pre-release).

## Mapa tabla → requerimiento que la ancla (12 tablas)

| Tabla | REQ principal | Otros REQ relacionados |
|---|---|---|
| `providers` | REQ-FUE-001 | REQ-FUE-002 (status/status_reason/status_changed_at) |
| `notif_channels` | REQ-NOT-002 | — |
| `users` | REQ-USU-001 (roles y status ACTIVE/INACTIVE) | REQ-USU-002 |
| `user_notification_channels` | REQ-NOT-001 | REQ-FE-005 (gestión desde la web) |
| `searches` | REQ-BUS-004 | REQ-BUS-002 (term_normalized), REQ-BUS-005 (check_frequency_hours) |
| `search_providers` | REQ-BUS-001 | REQ-DET-004/005 (last_run_at) |
| `provider_runs` | REQ-DET-005 | REQ-DET-003 (éxito/fallo de corrida) |
| `search_notifications` | REQ-NOT-001 | REQ-NOT-004 (borrador: enrutamiento futuro) |
| `events` | REQ-DET-001 | REQ-DET-003 (miss_count), REQ-BUS-003 (filtro previo a insertar) |
| `event_changes` | REQ-DET-001 | REQ-DET-002 (cualquier campo extraído alerta) |
| `notifications` | REQ-NOT-003 | REQ-FUE-002 (type PROVIDER_DISABLED, event_id NULL), REQ-FE-003/004 |
| `notifications_log` | REQ-NOT-003 | REQ-NOT-005 (envío por SendGrid) |

Última sincronización con la migración: 2026-07-04 (sesión final de requerimientos).
