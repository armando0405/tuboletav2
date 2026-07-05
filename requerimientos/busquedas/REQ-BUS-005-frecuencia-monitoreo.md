# REQ-BUS-005: Frecuencia de monitoreo elegida por el usuario

**Estado:** confirmado
**Tipo:** funcional
**Módulo:** busquedas
**Última actualización:** 2026-07-04
**Relacionado con:** REQ-DET-003, REQ-DET-004
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
Al crear (o editar) una búsqueda, el usuario elige cada cuánto se monitorea, entre presets cerrados: **cada 6, 12, 24 o 48 horas**. No es un valor libre. El default es 24 horas. Esta frecuencia se muestra siempre al usuario de forma explícita.

## Contexto / decisiones tomadas
- Decisión del usuario (2026-07-04), surgida al definir las desapariciones (REQ-DET-003): si "eliminado" depende de N corridas, el usuario necesita saber y controlar cada cuánto corre su búsqueda ("ese dato se lo debemos dar formalizado al usuario para que escoja").
- Presets cerrados (no valor libre) para proteger a los proveedores de frecuencias abusivas y simplificar el scheduler.
- Se modela como `searches.check_frequency_hours` con `CHECK (IN (6,12,24,48))`; el scheduler la combina con `search_providers.last_run_at` por par.

## Criterios de aceptación
- [ ] La UI de crear/editar búsqueda ofrece exactamente las 4 opciones.
- [ ] Un par búsqueda↔proveedor no se ejecuta antes de que pasen las horas configuradas desde su `last_run_at`.
