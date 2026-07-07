# Flujo del Scheduler de Monitoreo (explicación)

> Explica cómo funciona el monitoreo automático de TuBoleta v2 de punta a punta:
> qué tablas consulta, cómo sabe qué buscar y qué le falta, y dónde ver los logs.
> Implementa REQ-DET-004 (qué se salta) y REQ-DET-005 (el mecanismo).

## 1. Idea central: "la base de datos es la cola"

No hay una cola externa (ni Redis, ni RabbitMQ). El "trabajo pendiente" se
**deduce** de la propia base de datos cada minuto:

> Un par (búsqueda ↔ proveedor) está **vencido** si nunca se ha corrido, o si
> ya pasó su ventana de frecuencia desde la última corrida.

Es decir, la pregunta "¿qué me falta buscar?" se responde con una consulta SQL,
no con un flag de "pendiente/hecho". Esto se recalcula en cada ciclo.

## 2. Tablas que participan

| Tabla | Rol en el scheduler |
|-------|---------------------|
| `search_providers` | **La cola.** Cada fila es un par búsqueda↔proveedor. Su columna `last_run_at` es la que dice cuándo se corrió por última vez. `is_active` permite pausar el par. |
| `searches` | Trae `check_frequency_hours` (cada cuánto monitorear: 6/12/24/48) y `status` (ACTIVE/INACTIVE/DELETED). Solo se monitorean las ACTIVE. |
| `users` | Se salta si el usuario está INACTIVE. |
| `providers` | Trae `search_url` (la URL a scrapear, con `{term}`), `config` (JSONB: `rate_limit_ms`, `user_agent`, `timeout_ms`) y `status` (ACTIVE/DISABLED). Solo ACTIVE. |
| `provider_runs` | **Bitácora.** Una fila por cada corrida real (por grupo): inicio, fin, éxito, mensaje de error, ítems encontrados, pares aplicados. Aquí revisas qué pasó. |
| `events` / `event_changes` | Resultado de la detección: el histórico de eventos por par y el detalle de cada campo que cambió. |
| `notifications` / `notifications_log` | El hecho notificable (inbox) y la bitácora de entrega por canal (email). |

## 3. El ciclo completo, paso a paso

```
cada 60s (SCHEDULER_TICK_MS)
      │
      ▼
┌─────────────────────────────────────────────────────────────┐
│ MonitoringDispatcher.tick()                                  │
│  1. DueWorkSelector.selectDueWork(now)                       │
│       - SELECT pares candidatos (JOIN search_providers +     │
│         searches + users + providers) filtrando:             │
│         par activo + búsqueda ACTIVE + usuario ACTIVE +      │
│         proveedor ACTIVE                                      │
│       - en Java, deja solo los VENCIDOS:                     │
│         last_run_at == null  ||                              │
│         last_run_at + check_frequency_hours <= now           │
│       - AGRUPA por (provider_id, term_normalized)            │
│         → un DueGroup = 1 sola petición HTTP para N pares    │
│           que buscan el mismo término en el mismo proveedor  │
│  2. Reagrupa los DueGroup POR PROVEEDOR                       │
│  3. Manda 1 job por proveedor al pool (3 hilos):             │
│     paralelismo ENTRE proveedores, nunca dentro              │
└─────────────────────────────────────────────────────────────┘
      │  (por cada proveedor, en su hilo)
      ▼
┌─────────────────────────────────────────────────────────────┐
│ processProviderGroups(grupos)                                │
│  - procesa sus grupos SECUENCIALMENTE                        │
│  - pausa rate_limit_ms ENTRE peticiones (no antes de la 1ª)  │
└─────────────────────────────────────────────────────────────┘
      │  (por cada grupo)
      ▼
┌─────────────────────────────────────────────────────────────┐
│ MonitoringRunService.runGroup(grupo)   [NO transaccional]    │
│  1. busca el ProviderExtractor que soporta el proveedor      │
│  2. extractor.extract(provider, term)  ← HTTP a TuBoleta     │
│       (fuera de transacción: no retiene conexión JDBC)       │
│  3. por CADA par del grupo:                                  │
│       MonitoringPersistenceService.applyPair(par, extracción)│
│       [transaccional, aislado por par]                       │
│         → ChangeDetectionService.detect(...)  (NEW/CHANGED/  │
│           REMOVED, miss_count, guarda events/event_changes)  │
│         → NotificationService.notifyDetectedChanges(...)     │
│           (crea notifications + fan-out a destinos + email)  │
│  4. recordRun(...)  [transaccional, SIEMPRE]                 │
│       → inserta 1 fila en provider_runs                      │
│       → actualiza last_run_at = inicio, en TODOS los pares   │
└─────────────────────────────────────────────────────────────┘
```

## 4. ¿Cómo sabe qué ya buscó y qué le falta?

No hay un flag "hecho". La verdad vive en **`search_providers.last_run_at`**:

- Al terminar una corrida (con éxito **o** con fallo), `recordRun` pone
  `last_run_at = ahora` en todos los pares del grupo. **Siempre.** (REQ-DET-005
  punto 6: un proveedor caído no se reintenta cada minuto, espera su ventana.)
- En el siguiente tick, `DueWorkSelector` vuelve a calcular quién está vencido.
  Los que acaban de correr ya NO están vencidos (su `last_run_at + frecuencia`
  aún es futuro), así que se saltan solos. Los que nunca corrieron
  (`last_run_at == null`) siempre están vencidos → entran primero.
- Los grupos se ordenan por "el más vencido primero".

Entonces "cuáles le faltan" = "cuáles están vencidos ahora mismo", recalculado
cada 60s. No se pierde ni se duplica: si un proveedor todavía está procesándose
de un tick anterior, el set `providersInProgress` hace que el tick actual lo
omita (una sola instancia del backend, REQ-DET-005 punto 8).

## 5. Qué se salta (REQ-DET-004)

Un par NO se monitorea si: el par está inactivo (`is_active=false`), la búsqueda
está INACTIVE o DELETED, el usuario está INACTIVE, o el proveedor está DISABLED.
Todo esto se filtra en el `SELECT` de candidatos, antes de calcular vencimiento.

## 6. Una corrida es "exitosa" si...

`success = (la extracción no lanzó excepción) && (extraction.failedItems() == 0)
&& (ningún par falló al aplicar detección/notificación)`.

Solo una corrida **completamente exitosa** cuenta para el `miss_count` y para
marcar eventos como REMOVED (REQ-DET-003): si el scraping falló a medias, los
eventos ausentes NO se penalizan (regla anti falso-eliminado).

## 7. Dónde ver los logs

Con los logs INFO agregados, en la consola del backend verás por corrida:

```
Tick del scheduler: 1 grupo(s) vencido(s) en 1 proveedor(es)
Despachando proveedor id=1 con 1 grupo(s) al pool de monitoreo
Corrida iniciada: proveedor='TuBoleta' término='fucks news' (1 par(es) suscrito(s))
Extracción OK: proveedor='TuBoleta' término='fucks news' -> 3 ítem(es) extraído(s), 0 fallido(s)
Par id=5 búsqueda='fucks news': 3 cambio(s) detectado(s) -> generando notificaciones
Notificación 12 creada: tipo=NEW búsqueda='fucks news' usuario=2
Enviando email a 'test@x.com' (asunto: "...") vía SendGrid desde '...'
Email enviado a 'test@x.com' — SendGrid respondió estado 202
Entrega de notificación 12 por canal 'EMAIL' a 'test@x.com': OK
Corrida finalizada: proveedor='TuBoleta' término='fucks news' success=true pares_aplicados=1/1
```

Además, la tabla **`provider_runs`** guarda el registro persistente de cada
corrida (útil para revisar históricamente qué falló).

Nota: si no hay trabajo vencido, el tick loguea a nivel DEBUG (no ensucia la
consola cada minuto). Para verlo, sube el nivel del paquete
`com.tuboleta.backend.service.scheduler` a DEBUG.

## 8. Cómo probar rápido (frecuencia)

`check_frequency_hours` está en **horas** y con un CHECK que solo admite
6/12/24/48. Para probar en minutos:

- **No** puedes poner "5 minutos" (la columna es horas).
- Sí puedes hacer que una búsqueda corra en **cada tick** (~cada 60s) poniendo la
  frecuencia en `0`, pero primero hay que relajar el CHECK. En tu BD de pruebas:

  ```sql
  ALTER TABLE searches DROP CONSTRAINT searches_check_frequency_hours_check;
  UPDATE searches SET check_frequency_hours = 0 WHERE id = <tu_busqueda>;
  ```

  Con `0`, `last_run_at + 0h <= now` es siempre cierto → la búsqueda queda
  vencida en cada tick y corre cada ~60s. Para acelerar aún más el ciclo, baja
  la variable de entorno `SCHEDULER_TICK_MS` (ej. `15000` = 15s) al arrancar.
- Para volver a producción, re-crea la constraint:
  ```sql
  ALTER TABLE searches ADD CONSTRAINT searches_check_frequency_hours_check
      CHECK (check_frequency_hours IN (6, 12, 24, 48));
  ```

## 9. Mapa de archivos (para no perderse)

| Archivo | Qué hace |
|---------|----------|
| `config/SchedulingConfig.java` | `@EnableScheduling` + el pool de 3 hilos (`monitoringTaskExecutor`). |
| `service/scheduler/MonitoringDispatcher.java` | El `@Scheduled` cada 60s. Selecciona, agrupa por proveedor, despacha al pool, aplica rate-limit. **Solo orquesta.** |
| `service/scheduler/DueWorkSelector.java` | El "¿qué está vencido?": consulta candidatos y agrupa por (proveedor, término). |
| `service/scheduler/DueGroup.java` | Record: (proveedor, término normalizado, lista de pares). |
| `service/scheduler/MonitoringRunService.java` | Corre un grupo: extrae (HTTP, sin transacción) y coordina la aplicación por par. |
| `service/scheduler/MonitoringPersistenceService.java` | La parte transaccional: `applyPair` (detect+notify atómico por par) y `recordRun` (provider_runs + last_run_at). |
| `service/scheduler/Sleeper.java` / `ThreadSleeper.java` | La pausa de rate-limit (inyectable para testear sin esperas). |
| `service/extraction/TuBoletaScraperExtractor.java` | Arma la URL con `search_url` (`{term}` URL-encoded) y parsea el HTML con Jsoup. |
| `service/impl/ChangeDetectionServiceImpl.java` | NEW/CHANGED/REMOVED, `miss_count`, filtro de coincidencia. |
| `service/impl/NotificationServiceImpl.java` | Crea `notifications` y hace fan-out a destinos (email). |

## Config relevante (`application.yaml`)

```yaml
scheduler:
  enabled:   ${SCHEDULER_ENABLED:true}    # apaga el monitoreo si es false
  tick-ms:   ${SCHEDULER_TICK_MS:60000}    # cada cuánto corre el dispatcher
  pool-size: ${SCHEDULER_POOL_SIZE:3}      # hilos = proveedores en paralelo
```
