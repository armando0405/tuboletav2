# REQ-NOT-004: Enrutamiento de alertas por tipo y canal

**Estado:** borrador
**Tipo:** funcional
**Módulo:** notificaciones
**Última actualización:** 2026-07-04
**Requiere:** REQ-NOT-001

## Descripción
A futuro, el usuario podría querer que ciertos tipos de alerta lleguen solo por ciertos canales (ej. "cancelaciones por WhatsApp, todo lo demás solo al inbox"). **Hoy no se implementa**. La regla vigente ("todo tipo → todos los destinos activos") quedó formalizada en REQ-NOT-001 (confirmado); este REQ solo declara la evolución futura.

## Contexto / decisiones tomadas
- Pospuesto deliberadamente (duda #3 del borrador, pregunta 2). Diseño previsto cuando llegue: columna(s) de filtro en `search_notifications` — no requiere reestructura.
- Cobra sentido real cuando exista más de un canal activo (hoy solo EMAIL, ver REQ-NOT-002).

## Notas / preguntas abiertas
- Sin fecha. Revisar cuando se habilite el segundo canal (Telegram o WhatsApp).
