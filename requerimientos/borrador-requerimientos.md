# Borrador de requerimientos — TuBoleta v2 (HISTÓRICO)

> **Este documento quedó congelado el 2026-07-04**: todo su contenido fue migrado a los REQs formales por módulo (ver `00-INDICE.md`). Se conserva como histórico de cómo se tomaron las decisiones originales — no se actualiza más.
> Última actualización: 2026-07-04 (congelado).

## Visión general del alcance (ideas grandes en discusión)

Se identificaron 6 bloques de posible expansión para v2. **Se priorizó primero definir el modelo de datos**, porque es la base que soporta a los demás:

- **A. Multi-usuario real** — registro/login de autoservicio; cada usuario gestiona sus propias búsquedas.
- **B. Multi-proveedor** — no solo TuBoleta: Ticketmaster, Eventbrite, etc.
- **C. Más canales de notificación** — Telegram y WhatsApp además de email (ya contemplados como inactivos en `notif_channels`).
- **D. Frontend de autoservicio** — un frontend para usuarios finales (hoy el admin Vue es solo gestión interna).
- **E. Operación y confiabilidad** — observabilidad del scheduler, resiliencia por proveedor, respeto a rate-limits.
- **F. Ideas más ambiciosas (futuro, sin decidir aún)** — seguimiento de precios, API pública/webhooks, app móvil/PWA.

Los bloques A–D son la base inmediata que el modelo de datos debe soportar. E y F quedan como roadmap para después, sin diseño concreto todavía.

## Decisiones acordadas sobre el modelo de datos

Estas decisiones surgieron de una serie de preguntas dirigidas; se listan pregunta → decisión para poder rastrear el porqué:

1. **Heterogeneidad de proveedores**: se espera que futuros proveedores sean muy distintos entre sí (scraping HTML vs API oficial con auth). → `providers` necesita un campo de configuración flexible (tipo JSONB), no solo `base_url`/`search_url` fijos. Se agrega también `provider_type` (`SCRAPER` / `API`).

2. **Roles de usuario**: el panel admin (Vue) seguirá existiendo, pero ahora convive con usuarios de autoservicio. → se agrega `role` (`ADMIN` / `USER`) a la tabla `users`, en vez de mantenerlos como sistemas separados.

3. **Planes/suscripciones**: por ahora **no** se preparan planes ni límites (no hay tabla `plans`). Todos los usuarios tienen las mismas capacidades sin restricción. Se dejará para una migración futura (V3/V4...) cuando haya necesidad real de monetizar.

4. **Autenticación**: se mantiene básica por ahora — solo login con email + `password_hash`. Verificación de email y recuperación de contraseña **no** se modelan todavía; se agregarán cuando haya envío de correo transaccional listo.

5. **Destinos de notificación**: se normalizan. En vez de que `search_notifications` guarde el `destination` repetido por cada búsqueda, se crea una tabla `user_notification_channels` (usuario + canal + destino) reutilizable entre búsquedas. `search_notifications` pasa a referenciar ese registro en vez de guardar el destino directamente.

6. **Relación búsqueda↔proveedor**: una búsqueda del usuario (ej. "Bad Bunny") puede monitorearse en **varios proveedores a la vez**, sin duplicar el término. Se modela como relación muchos-a-muchos: `searches` deja de tener `provider_id` directo, y se agrega una tabla intermedia `search_providers` (search_id, provider_id, is_active, last_run_at).

## Cambios de esquema (CONFIRMADOS e implementados en `V1__init.sql` — 2026-07-01)

Sobre el esquema actual (`V1__init.sql`, ver `CLAUDE.md` y `version-1-analisis.md` para el contraste con v1):

- `providers`: **+** `config` (JSONB), **+** `provider_type` (`SCRAPER`/`API`).
- `users`: **+** `role` (`ADMIN`/`USER`).
- `searches`: **–** `provider_id` (se mueve a la tabla intermedia); queda `(user_id, name, term, status)`.
- **Nueva** `search_providers`: `search_id`, `provider_id`, `is_active`, `last_run_at` — cada proveedor de una búsqueda corre/se pausa de forma independiente.
- **Nueva** `user_notification_channels`: `user_id`, `channel_id`, `destination`, `is_active`.
- `search_notifications`: cambia de guardar `destination` directo a referenciar `user_notification_channel_id`.
- `events`: pasa a referenciar `search_provider_id` en vez de `search_id`; el `UNIQUE(search_id, external_id)` pasa a `UNIQUE(search_provider_id, external_id)` para evitar colisiones de `external_id` entre proveedores distintos.

**Estado**: CONFIRMADO por el usuario e implementado el 2026-07-01 reescribiendo por completo `tuboleta-backend/src/main/resources/db/migration/V1__init.sql` (v2 no hereda nada de v1, así que se consolidó en una sola V1 limpia en vez de parchear con un `V2__`).

### Decisiones adicionales tomadas al escribir la migración (no estaban en el borrador original)

- **`TIMESTAMPTZ` en vez de `TIMESTAMP`** en todas las columnas de fecha/hora, para evitar ambigüedad de zona horaria (v1 asumía `America/Bogota` implícitamente). `event_date` sigue siendo `DATE` (fecha calendario del evento).
- **PK con `BIGINT GENERATED ALWAYS AS IDENTITY`** (estándar SQL moderno) en vez de `BIGSERIAL`.
- **`searches.last_run_at` se elimina**; el "última corrida" ahora es `search_providers.last_run_at`, porque cada par búsqueda↔proveedor corre de forma independiente.
- **`providers.config` (JSONB) se sembró con un ejemplo** para TuBoleta (`user_agent`, `timeout_ms`, `rate_limit_ms`), mostrando cómo la config flexible reemplaza el YAML disperso de v1.
- **`notifications_log.destination` se guarda como snapshot de texto** (no como FK a `user_notification_channels`), para que el log de auditoría conserve a dónde se envió realmente aunque el destino se edite o borre después.

## Pendientes / próximos pasos

- Confirmar el diseño de esquema anterior (o ajustarlo) antes de pasar a plan de implementación.
- Diseñar en detalle, uno a la vez y por separado, los bloques B (multi-proveedor: cómo se implementa el adapter por tipo de proveedor), C (canales Telegram/WhatsApp) y D (frontend de autoservicio) — quedan pendientes de spec propio.
- E y F quedan solo como ideas de roadmap, sin detalle todavía.
