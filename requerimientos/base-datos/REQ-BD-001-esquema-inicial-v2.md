# REQ-BD-001: Esquema inicial v2

**Estado:** implementado
**Tipo:** técnico
**Módulo:** base-datos
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-BD-002, REQ-BD-003
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
El esquema relacional completo de v2 vive en la migración Flyway `tuboleta-backend/src/main/resources/db/migration/V1__init.sql` (PostgreSQL). Tablas: `providers`, `notif_channels`, `users`, `user_notification_channels`, `searches`, `search_providers`, `search_notifications`, `events`, `event_changes`, `notifications`, `notifications_log`. Es una reestructuración total respecto a v1: multi-usuario, multi-proveedor y multi-canal desde el diseño.

## Contexto / decisiones tomadas
- v1 era single-tenant sobre H2 con `ddl-auto: create` (pérdida de datos en cada reinicio) — ver `version-1-analisis.md`.
- Las decisiones de diseño originales están en `borrador-requerimientos.md` (histórico): N:M búsqueda↔proveedor, destinos de notificación normalizados, roles en tabla única.
- Convenciones: `TIMESTAMPTZ` para fecha/hora (v1 asumía zona Bogotá implícita), PK `BIGINT GENERATED ALWAYS AS IDENTITY`, borrados siempre lógicos.

## Criterios de aceptación
- [ ] La migración aplica limpia sobre PostgreSQL 16 vacío (Flyway al arrancar el backend).
- [ ] Cada tabla del esquema está anclada a un REQ confirmado (mapa en `artefactos/esquema-bd.md`).

## Notas / preguntas abiertas
- El estado explícito del proveedor (REQ-FUE-002, en borrador) tocará este esquema cuando se confirme.
