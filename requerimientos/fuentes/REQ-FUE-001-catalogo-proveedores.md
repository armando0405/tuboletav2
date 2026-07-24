# REQ-FUE-001: Catálogo de proveedores con método de extracción configurable

**Estado:** confirmado
**Tipo:** técnico
**Módulo:** fuentes
**Última actualización:** 2026-07-04
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
Cada fuente (proveedor de boletas) se registra en `providers` con un `provider_type` explícito — `SCRAPER` (se parsea HTML) o `API` (se consume una API que devuelve JSON) — y un campo `config` (JSONB) con su configuración específica: user-agent, timeouts, rate-limit, selectores, endpoints, etc. Ambos métodos normalizan sus resultados al mismo formato interno (la tabla `events`). El método lo configura el equipo de desarrollo manualmente cuando la lógica de esa fuente está lista; el sistema nunca lo decide dinámicamente.

## Contexto / decisiones tomadas
- Cierra la duda #4 del borrador; decisión previa (histórico #1) sobre heterogeneidad de proveedores.
- **No-objetivo explícito**: no se conserva historial de qué método se usó en qué momento. Si una fuente pasa de SCRAPER a API, se reemplaza; `events.raw_json` ya preserva el payload de origen de cada evento y el historial de git de la migración/seed cubre lo forense. Que no resucite.
- Se agrega `providers.updated_at` para saber cuándo se editó la config por última vez (más barato que cualquier historial).
- **Deslinde columnas vs config**: `base_url` y `search_url` (la URL de búsqueda con plantilla `{term}`) son columnas de primera clase porque todo proveedor las tiene; el resto de la configuración específica (user-agent, timeouts, rate-limit, selectores, headers, endpoints extra) vive en `config` JSONB.

## Criterios de aceptación
- [ ] Agregar un proveedor nuevo no requiere cambio de esquema (solo fila + config + código de su extractor).
- [ ] TuBoleta sembrado como `SCRAPER` con config de ejemplo.
- [ ] Los eventos de un proveedor API y uno SCRAPER son indistinguibles en `events` (mismo contrato).
