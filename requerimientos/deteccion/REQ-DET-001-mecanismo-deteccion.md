# REQ-DET-001: Mecanismo de detección de cambios

**Estado:** implementado
**Tipo:** técnico
**Módulo:** deteccion
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-DET-002, REQ-DET-003
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
La identidad de un evento es su `external_id` (identificador estable en la fuente, ej. el "link" de TuBoleta) dentro de un par búsqueda↔proveedor. En cada corrida: `external_id` nunca visto → evento **NEW**; ya visto → **diff campo a campo** contra lo almacenado (cualquier campo extraído que cambie genera fila en `event_changes` y dispara **CHANGED**, ver REQ-DET-002); deja de aparecer → **REMOVED** según las reglas de REQ-DET-003. **Reaparición tras REMOVED**: si un `external_id` ya marcado REMOVED vuelve a aparecer, el evento regresa a `ACTIVE` con `miss_count = 0` y se notifica como **NEW** (para el usuario, "el show volvió"). **No se usa hash** — ni global por snapshot (v1) ni por evento.

Campos de soporte de `events`: `event_date` es la fecha parseada best-effort de `event_date_raw` (derivada, nullable, para ordenar/filtrar — no participa del diff); `first_seen_at`/`last_seen_at` marcan la ventana de vida observada del evento.

## Contexto / decisiones tomadas
Resuelve la duda de la nota de voz ("¿el enfoque de hash está bien o hay otra forma?"):
- El hash MD5 global de v1 era frágil (identidad título+venue+ciudad rompía ante renombres) y solo comparaba 2 campos.
- El hash SHA-256 por evento del diseño inicial de v2 se **elimina** por redundante: para llenar `event_changes` hay que comparar campo a campo de todas formas; con 5–50 eventos por búsqueda el short-circuit no ahorra nada medible; solo cubría 3 campos (cambios en el resto serían invisibles); y ampliar el hash marcaría todos los eventos existentes como "modificados" en la siguiente corrida.
- Alternativas evaluadas y descartadas: CDC (no hay BD upstream), volver a snapshots comparados (regresión a v1).

## Criterios de aceptación
- [ ] `events` sin columna `hash`.
- [ ] Un cambio en cualquier campo comparado genera exactamente una fila en `event_changes` con valor viejo y nuevo.
- [ ] El mismo `external_id` en dos proveedores distintos son dos eventos independientes (sin colisión).
