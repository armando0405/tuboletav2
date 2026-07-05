# REQ-DET-004: Scheduler de monitoreo

**Estado:** confirmado
**Tipo:** técnico
**Módulo:** deteccion
**Última actualización:** 2026-07-04
**Requiere:** REQ-BUS-005
**Relacionado con:** REQ-BUS-001, REQ-BUS-004, REQ-DET-003, REQ-DET-005

## Descripción
El scheduler ejecuta el ciclo de monitoreo por **par búsqueda↔proveedor**, respetando la frecuencia elegida por el usuario (`searches.check_frequency_hours`) contra el `last_run_at` del par. Salta: pares con `is_active = false`, búsquedas `INACTIVE`/`DELETED` (regla de REQ-BUS-004), usuarios `INACTIVE` y proveedores `DISABLED` (verificado en runtime, sin cascada). El **mecanismo** de ejecución (dispatcher, agrupación, concurrencia, log de corridas) está definido en REQ-DET-005.

## Contexto / decisiones tomadas
- Lo confirmado aquí es el **comportamiento** (qué corre, cuándo y qué salta). El mecanismo interno (cron vs fixed-rate vs cola de trabajos) es decisión de implementación posterior — v1 usaba fixed-rate fijo global (3–15 min), que desaparece: ahora la frecuencia es por búsqueda y mucho más espaciada (6–48 h).
- El keep-alive de v1 (ping a sí mismo para que Render.com no durmiera la instancia) NO se hereda; se reevaluará solo si el hosting elegido lo exige.

## Criterios de aceptación
- [ ] Ningún par corre antes de cumplir su frecuencia desde su `last_run_at`.
- [ ] Deshabilitar un proveedor detiene sus corridas sin tocar las búsquedas ni otros pares.
- [ ] Cada corrida ejecutada actualiza `last_run_at` del par y deja su resultado (éxito/fallo) en `provider_runs` (REQ-DET-005).

## Notas / preguntas abiertas
- Reintentos con backoff dentro de la ventana de frecuencia: mejora futura declarada en REQ-DET-005 (hoy, un fallo espera a la siguiente ventana).
