# REQ-DET-002: Campos que disparan alerta de "modificado"

**Estado:** confirmado
**Tipo:** funcional
**Módulo:** deteccion
**Última actualización:** 2026-07-04
**Requiere:** REQ-DET-001

## Descripción
**Cualquier campo extraído** de un evento que cambie entre corridas dispara la alerta CHANGED: hoy son `title`, `venue` y `event_date_raw`, y si a futuro se extraen más columnas (ciudad, URL, precio...), también alertan automáticamente al incorporarse. Cada campo cambiado genera su fila en `event_changes`, pero **una corrida genera UNA sola notificación CHANGED por evento**, aunque hayan cambiado varios campos a la vez (la notificación agrupa; el detalle campo a campo vive en `event_changes`).

## Contexto / decisiones tomadas
- Decisión del usuario (2026-07-04): eligió "todo lo extraído" sobre las alternativas (solo venue+fecha como v1, o solo title/venue/fecha fijos). Cubre de sobra la deuda de v1, que ignoraba cambios de título.
- `event_date` (DATE parseada) es un **derivado** de `event_date_raw`: no se compara por sí misma — si la fecha cambia, cambia el raw y eso ya alerta. Datos que solo viven dentro de `raw_json` sin columna propia no alertan (no son "extraídos"); extraer uno nuevo = agregar columna + alerta automática.
- Granularidad (resuelve hallazgo de la revisión de consistencia): N campos cambiados → N filas en `event_changes`, 1 notificación CHANGED.

## Criterios de aceptación
- [ ] Un cambio solo de título genera alerta CHANGED (en v1 era invisible).
- [ ] Cambian título y venue en la misma corrida → 2 filas en `event_changes`, 1 notificación.
- [ ] Un cambio interno de `raw_json` sin cambio en columnas extraídas no genera alerta.
