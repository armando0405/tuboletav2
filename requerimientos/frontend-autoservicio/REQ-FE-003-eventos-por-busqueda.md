# REQ-FE-003: Vista de eventos por búsqueda con destaque de novedades

**Estado:** borrador
**Tipo:** funcional
**Módulo:** frontend-autoservicio
**Última actualización:** 2026-07-04
**Requiere:** REQ-NOT-003
**Relacionado con:** REQ-DET-001
**Artefactos relacionados:** artefactos/diseno-frontend.md, artefactos/patron-frontend.md

## Descripción
Dentro de una búsqueda, el usuario ve los eventos detectados (activos y removidos) con sus datos (título, venue, fecha, link a la fuente). Los eventos **nuevos o cambiados** desde su última visita se destacan visualmente — no basta con que llegue el correo: el panel debe reflejar la novedad.

## Contexto / decisiones tomadas
- Surge de la nota de voz: "le va a llegar el mensaje, pero también en el dashboard tiene que aparecerle el nuevo show".
- El destaque se apoya en el modelo `notifications` + `read_at` (REQ-NOT-003): novedad = notificación no leída asociada a ese evento. No se inventa un mecanismo aparte.

## Notas / preguntas abiertas
- ¿Los eventos REMOVED se muestran tachados/apagados o en pestaña aparte? Por definir en diseño.
