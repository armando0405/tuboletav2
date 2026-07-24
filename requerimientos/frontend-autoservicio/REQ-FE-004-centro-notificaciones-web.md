# REQ-FE-004: Centro de notificaciones en la web

**Estado:** borrador
**Tipo:** funcional
**Módulo:** frontend-autoservicio
**Última actualización:** 2026-07-04
**Requiere:** REQ-NOT-003
**Artefactos relacionados:** artefactos/diseno-frontend.md, artefactos/patron-frontend.md

## Descripción
Sección de la web donde el usuario ve el histórico de sus notificaciones (evento nuevo / cambiado / eliminado, por cuál búsqueda, cuándo), con estado leído/no leído y contador de pendientes. Es la vista sobre la tabla `notifications` (REQ-NOT-003).

## Contexto / decisiones tomadas
- Surge de la nota de voz: "que esté la sección del usuario [donde] diga: llegó notificación para tal... aparte de que llega el correo".

## Notas / preguntas abiertas
- ¿Marcar todas como leídas de un golpe? ¿Filtros por búsqueda/tipo? Por definir en diseño.
- ¿Se muestran también los intentos de entrega por canal (falló el correo)? Probablemente solo para ADMIN — por definir.
