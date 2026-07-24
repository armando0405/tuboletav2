# REQ-FE-005: Gestión de destinos de notificación

**Estado:** borrador
**Tipo:** funcional
**Módulo:** frontend-autoservicio
**Última actualización:** 2026-07-04
**Requiere:** REQ-NOT-001
**Relacionado con:** REQ-FE-001
**Artefactos relacionados:** artefactos/diseno-frontend.md, artefactos/patron-frontend.md

## Descripción
Pantalla/sección donde el usuario gestiona sus destinos de notificación (`user_notification_channels`): registrar varios correos, activar/desactivar o eliminar cada destino. Además, al crear o editar una búsqueda (REQ-FE-001), el usuario elige cuáles de sus destinos aplican a esa búsqueda (`search_notifications`).

## Contexto / decisiones tomadas
- Surge de la revisión de consistencia (2026-07-04): REQ-NOT-001 (confirmado) exige "registrar 2+ correos y elegir cuáles aplican a cada búsqueda", pero ningún REQ-FE lo cubría — la búsqueda se habría creado sin poder decidir a dónde notifica.
- Si el usuario no tiene ningún destino registrado al crear su primera búsqueda, el flujo debe llevarlo a crear uno (no dejar una búsqueda sin destino silenciosamente).

## Notas / preguntas abiertas
- ¿La gestión de destinos vive en el perfil del usuario o como paso dentro del flujo de crear búsqueda? Por definir en diseño de UI.
- Verificación del destino (confirmar que el correo existe/le pertenece): pospuesta junto con la verificación de email de REQ-USU-002.
