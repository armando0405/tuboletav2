# REQ-NOT-001: Destinos de notificación por usuario (1:N, reutilizables)

**Estado:** implementado
**Tipo:** funcional
**Módulo:** notificaciones
**Última actualización:** 2026-07-06
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
Cada usuario define sus destinos de notificación (canal + dirección) en `user_notification_channels`: puede tener varios, incluyendo **varios correos distintos**. Una búsqueda se asocia a uno o más de esos destinos vía `search_notifications`; el mismo destino se reutiliza entre búsquedas sin duplicarlo. La asociación búsqueda↔destino puede desactivarse (`search_notifications.is_active`) sin borrar el destino ni afectar otras búsquedas.

**Regla de enrutamiento vigente**: toda notificación de una búsqueda se envía a TODOS sus destinos activos, sin distinción por tipo de alerta (el enrutamiento por tipo/canal es futuro declarado, REQ-NOT-004).

## Contexto / decisiones tomadas
- Cierra la duda #3 del borrador y consolida la decisión previa (histórico #5: normalizar destinos).
- Supera el modelo de v1: un único destinatario hardcodeado en YAML para todo el sistema.
- `UNIQUE(user_id, channel_id, destination)` impide duplicar el mismo destino; `destination` es email, chat_id o teléfono según el canal.

## Criterios de aceptación
- [ ] Un usuario puede registrar 2+ correos y elegir cuáles aplican a cada búsqueda.
- [ ] Editar/desactivar un destino afecta a todas las búsquedas que lo usan (es una sola fila).
