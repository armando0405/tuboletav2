# REQ-DET-005: Mecanismo de ejecución del scheduler — la BD es la cola

**Estado:** confirmado
**Tipo:** técnico
**Módulo:** deteccion
**Última actualización:** 2026-07-04
**Requiere:** REQ-BUS-005, REQ-DET-004
**Relacionado con:** REQ-DET-003, REQ-FUE-001, REQ-ARQ-004
**Artefactos relacionados:** artefactos/esquema-bd.md

## Descripción
No existe una cola externa (RabbitMQ/Kafka) ni tabla de cola: **el trabajo pendiente se deriva de los datos**. Un par búsqueda↔proveedor está "vencido" cuando `last_run_at IS NULL` (nunca corrió) o `NOW() >= last_run_at + check_frequency_hours`, con todos los filtros de actividad (par activo, búsqueda ACTIVE, usuario ACTIVE, proveedor ACTIVE). Un único dispatcher `@Scheduled` (tick cada ~60s) consulta los pares vencidos y los procesa; nada se pierde jamás: lo que no se alcance a procesar en un tick sigue vencido y lo toma el siguiente — la "cola" se reconstruye sola desde la BD incluso tras un reinicio del backend.

## Contexto / decisiones tomadas
Responde la duda central del usuario ("¿cómo se maneja que a 10-15 usuarios se les cumpla el tiempo a la vez? ¿cómo se encola? ¿se pierde una petición? ¿cuántos hilos?"). El usuario delegó explícitamente el diseño ("la mejor solución que tú veas conveniente es la que vamos a aplicar", 2026-07-04). Diseño:

1. **Dispatcher único** (`@Scheduled`, fixedDelay ~60s): consulta pares vencidos ordenados por "más vencido primero". Es solo orquestación (REQ-ARQ-004) — la lógica vive en servicios.
2. **Deduplicación por grupo**: los pares vencidos se agrupan por `(provider_id, term_normalized)` → **UNA sola petición HTTP por grupo**, cuyo resultado se aplica a TODOS los pares suscritos (el diff sí es individual por par, porque cada par tiene su propio histórico en `events`). Ejemplo: 3 usuarios buscando "fucks news" en TuBoleta = 1 scraping, 3 diffs. Esto absorbe los picos.
3. **Concurrencia**: pool fijo de trabajo (≈3 hilos). Paralelismo ENTRE proveedores distintos; DENTRO del mismo proveedor las peticiones van secuenciales con pausa `rate_limit_ms` (del `config` del proveedor) para no bombardear la fuente.
4. **Registro de corridas — tabla `provider_runs`**: una fila por petición real (proveedor, término, inicio/fin, éxito, error, eventos encontrados, pares aplicados). Da observabilidad ("¿qué corrió, cuándo, cómo terminó?") y es el insumo de "corrida exitosa vs fallida" que exige REQ-DET-003.
5. **Definición de corrida exitosa**: la petición Y la extracción de TODOS los ítems terminaron sin error. Si algún ítem individual falla al extraerse, la corrida se marca `success = false`: los eventos que sí se extrajeron **se procesan** (NEW/CHANGED aplican), pero la corrida **no incrementa `miss_count` ni genera REMOVED** — así una extracción parcial nunca produce un falso "se canceló" (cierra el hueco detectado en revisión: v1 descartaba ítems fallidos con WARN y habría contado ausencias falsas).
6. **`last_run_at` se actualiza siempre al ejecutar** (éxito o fallo): un proveedor caído no se bombardea cada tick; el par reintenta en su siguiente ventana de frecuencia. Reintentos con backoff dentro de la ventana: mejora futura, fuera de alcance.
7. **Primera corrida inmediata**: una búsqueda recién creada tiene `last_run_at NULL` → el próximo tick la toma (≤ ~60s). Buen feedback para el usuario.
8. **Restricción declarada**: el backend corre en UNA sola instancia; sin lock distribuido. Si algún día hay réplicas, se añade (ej. ShedLock) — decisión consciente, no olvido.

## Criterios de aceptación
- [ ] Matar y reiniciar el backend a mitad de un tick no pierde ningún par: los vencidos se retoman en el siguiente tick.
- [ ] Dos búsquedas del mismo término y proveedor vencidas a la vez generan exactamente 1 fila en `provider_runs` con `pairs_applied = 2`.
- [ ] Dos proveedores distintos con trabajo vencido se procesan en paralelo; dos términos del mismo proveedor, en secuencia con la pausa configurada.
- [ ] Una corrida con un ítem inextraíble queda `success = false`, procesa los ítems buenos y no toca `miss_count` de nadie.
