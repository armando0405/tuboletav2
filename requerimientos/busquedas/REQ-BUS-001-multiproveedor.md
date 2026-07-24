# REQ-BUS-001: Búsqueda multi-proveedor (N:M)

**Estado:** implementado
**Tipo:** funcional
**Módulo:** busquedas
**Última actualización:** 2026-07-06
**Requiere:** REQ-FUE-001
**Relacionado con:** REQ-DET-004
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
Una búsqueda es un término del usuario (ej. "Bad Bunny") que puede monitorearse en uno o varios proveedores a la vez, sin duplicar el término. Se modela con la tabla intermedia `search_providers`; cada par búsqueda↔proveedor se activa/pausa y registra su `last_run_at` de forma independiente.

## Contexto / decisiones tomadas
- Decisión previa (histórico #6). Supera el diseño de v1 (un solo proveedor hardcodeado) y el diseño inicial de v2 (FK directa `searches.provider_id`).
- Los eventos cuelgan del par (`events.search_provider_id`), de modo que el mismo evento hallado en dos proveedores son dos filas independientes.

## Criterios de aceptación
- [ ] `UNIQUE(search_id, provider_id)` en `search_providers`.
- [ ] Pausar un par no afecta a los otros proveedores de la misma búsqueda.
