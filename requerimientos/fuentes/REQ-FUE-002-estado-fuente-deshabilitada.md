# REQ-FUE-002: Estado visible cuando una fuente se deshabilita

**Estado:** implementado
**Tipo:** funcional
**Módulo:** fuentes
**Última actualización:** 2026-07-06
**Requiere:** REQ-NOT-003
**Relacionado con:** REQ-USU-001, REQ-FE-002
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
Cuando un ADMIN deshabilita una fuente (ej. por fallas repetidas), los usuarios con búsquedas sobre ella se enteran: el estado es visible en su panel y les llega una notificación proactiva — nunca un apagado interno silencioso. Modelo: `providers.status` con dos valores (`ACTIVE` / `DISABLED`) + `status_reason` (texto libre que explica el porqué y el matiz temporal/permanente) + `status_changed_at`. La notificación usa el tipo `PROVIDER_DISABLED` en `notifications`: **una por búsqueda afectada** (no por par), con `event_id NULL`. Reactivación: solo ADMIN.

## Contexto / decisiones tomadas
- Confirmado por el usuario el 2026-07-04 tal como se propuso (descartó la variante PAUSED/DISABLED de dos estados: la promesa "volverá" no es sostenible; el texto libre comunica mejor el matiz).
- Reemplaza el booleano `is_active` original de `providers` por el estado explícito (aplicado en `V1__init.sql` bajo la regla de editabilidad de REQ-BD-003).
- No confundir niveles: `search_providers.is_active` (pausa de un par, decisión del usuario) y `providers.status` (estado global, decisión del ADMIN) conviven; el frontend muestra el estado **efectivo** del par (REQ-FE-002).
- `notifications.event_id` es nullable precisamente para este tipo sin evento.

## Criterios de aceptación
- [ ] Deshabilitar una fuente genera una notificación PROVIDER_DISABLED por cada búsqueda activa que la usaba (inbox + canales activos).
- [ ] El scheduler deja de ejecutar pares de esa fuente inmediatamente, sin tocar las búsquedas.
- [ ] El listado de búsquedas del usuario muestra la fuente como deshabilitada con su razón.
- [ ] Un USER no puede cambiar `providers.status`; un ADMIN sí (ambos sentidos).
