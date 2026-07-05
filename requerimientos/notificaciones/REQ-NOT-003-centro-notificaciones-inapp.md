# REQ-NOT-003: Centro de notificaciones in-app (inbox)

**Estado:** confirmado
**Tipo:** funcional
**Módulo:** notificaciones
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-FE-003, REQ-FE-004
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
Además del correo, el usuario ve dentro de la web un histórico de sus notificaciones (inbox), con estado leído/no leído. Se modela en dos niveles: la tabla `notifications` guarda el **hecho notificable** (usuario, búsqueda, evento —nullable—, tipo NEW/CHANGED/REMOVED/PROVIDER_DISABLED, `created_at`, `read_at` nullable — leída = `read_at` no nulo); `notifications_log` pasa a ser el **log de entregas por canal** (FK a la notificación + canal + destino snapshot + resultado). Este mismo modelo alimenta el destaque visual de "nuevo/cambiado" en el dashboard.

**Semántica de "leída"**: una notificación se marca leída de forma **individual** (al abrirla/verla en detalle) o con una acción explícita "marcar todas como leídas" — nunca automáticamente por el solo hecho de visitar el inbox. Así el destaque del dashboard (REQ-FE-003), que usa el mismo `read_at`, no se apaga sin que el usuario haya visto realmente la novedad.

## Contexto / decisiones tomadas
- Surge de la nota de voz (2026-07-04): "aparte que llega el correo... que en la página web esté la sección del usuario [con] la notificación" y "también en el panel tiene que aparecerle el nuevo show".
- Se descartó el diseño alternativo (flag de leído sobre `notifications_log`): el log es auditoría de envíos por canal — mezclaría estado mutable con auditoría, duplicaría la tupla (user, search, event, type) por cada canal, y una notificación puede existir en el inbox **sin haberse enviado por ningún canal** (cero filas de entrega, una fila de notificación). También se descartó inventar un canal ficticio IN_APP.
- Reestructura aplicada directamente en `V1__init.sql` bajo la regla de editabilidad de REQ-BD-003.

## Criterios de aceptación
- [ ] Una notificación sin entregas es visible en el inbox.
- [ ] Marcar como leída solo escribe `read_at`; no toca el log de entregas.
- [ ] Cada envío por canal genera exactamente una fila de entrega ligada a su notificación.
- [ ] El contador de "no leídas" del usuario sale de `read_at IS NULL`.
