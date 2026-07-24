# REQ-NOT-002: Email como único canal activo

**Estado:** implementado
**Tipo:** funcional
**Módulo:** notificaciones
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-NOT-005

## Descripción
El único canal de notificación implementado en esta versión es el correo electrónico. `notif_channels` se siembra con EMAIL (activo), TELEGRAM y WHATSAPP (inactivos): deuda técnica consciente para habilitarlos a futuro sin cambio de esquema ni rehacer la lógica.

## Contexto / decisiones tomadas
- Ratifica el diseño original de v2 y la duda #3 del borrador ("por ahora solo correo, con espacio para Telegram/WhatsApp").
- El diseño de envío debe quedar detrás de una abstracción por canal, de modo que agregar Telegram sea agregar una implementación, no tocar el flujo.

## Criterios de aceptación
- [ ] Seed con exactamente 3 filas; solo EMAIL con `is_active = true`.
- [ ] Intentar registrar un destino sobre un canal inactivo → rechazado con mensaje claro.
