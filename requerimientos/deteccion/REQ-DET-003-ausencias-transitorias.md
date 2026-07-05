# REQ-DET-003: Ausencias transitorias y falsos "eliminados"

**Estado:** confirmado
**Tipo:** funcional
**Módulo:** deteccion
**Última actualización:** 2026-07-04
**Requiere:** REQ-DET-001
**Relacionado con:** REQ-BUS-005
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
Un evento se marca `REMOVED` (y se notifica al usuario) únicamente tras **N = 2 corridas exitosas consecutivas** del par búsqueda↔proveedor en las que el evento no aparece. Una corrida fallida (error de scraping, timeout, HTML cambiado) **nunca** incrementa el contador ni genera `REMOVED` — el resultado de una corrida fallida se descarta por completo para efectos de desapariciones.

## Contexto / decisiones tomadas
- Hallazgo del revisor técnico (2026-07-04), confirmado por el usuario: un falso "el show se canceló" causado por un fallo parcial del scraping es el peor bug de UX posible en este dominio. v1 no distinguía corrida fallida de corrida sin resultados (aunque abortaba el ciclo completo ante fallo, no dejaba rastro).
- Se modela con `events.miss_count` (contador de corridas exitosas consecutivas sin ver el evento): reaparece → `miss_count = 0` y sigue `ACTIVE`; llega a N → `REMOVED` + notificación.
- **Qué es una "corrida exitosa"** (definido en REQ-DET-005): la petición Y la extracción de TODOS los ítems sin error. Una corrida con extracción parcial se marca fallida en `provider_runs`: sus eventos extraídos sí se procesan (NEW/CHANGED), pero no incrementa `miss_count` — una extracción a medias jamás produce un falso "eliminado".
- El resultado de cada corrida queda persistido en la tabla `provider_runs` (REQ-DET-005), no solo en logs.
- N=2 como default; combinado con la frecuencia elegida (REQ-BUS-005), el usuario sabe cuánto tarda como máximo en enterarse de una cancelación real (ej. frecuencia 12h → aviso a las ~24h).

## Criterios de aceptación
- [ ] Corrida fallida → ningún `miss_count` cambia, ningún evento pasa a REMOVED.
- [ ] Evento ausente en 1 corrida exitosa y presente en la siguiente → sigue ACTIVE, contador en 0, sin notificación.
- [ ] Evento ausente en 2 corridas exitosas consecutivas → REMOVED + notificación una sola vez.
