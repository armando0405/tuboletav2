-- ============================================================
-- TU_BOLETA V2 — Migración inicial (esquema completo)
-- Flyway: V1__init.sql   |   Motor: PostgreSQL
-- Fecha: 2026-07
-- ============================================================
-- REGLA DE EDITABILIDAD (REQ-BD-003): este archivo ES editable
-- mientras no exista un despliegue compartido; toda edición
-- implica recrear la BD local (docker-compose down -v).
-- Desde el primer despliegue compartido queda CONGELADO y todo
-- cambio posterior va en V2__xxx.sql, V3__..., etc.
-- Definición conceptual del esquema: carpeta requerimientos/
-- (mapa tabla → REQ en requerimientos/artefactos/esquema-bd.md).
-- ============================================================
-- Este esquema es una reestructuración total respecto a la v1
-- (proyecto tuboleta_scraper_api-main, ver requerimientos/version-1-analisis.md):
--   - v1 era single-tenant (una sola búsqueda "fucks news"
--     hardcodeada) sobre H2 embebida con ddl-auto=create.
--   - v2 es multi-usuario, multi-proveedor y multi-canal sobre
--     PostgreSQL con migraciones versionadas (sin pérdida de datos).
-- Decisiones de diseño en requerimientos/borrador-requerimientos.md.
-- Se usa TIMESTAMPTZ (no TIMESTAMP) para evitar ambigüedad de zona
-- horaria; v1 asumía America/Bogota de forma implícita.
-- ============================================================


-- ============================================================
-- PROVIDERS — Catálogo de plataformas de boletas
-- ============================================================
-- status (REQ-FUE-002): ACTIVE / DISABLED. DISABLED → el scheduler
-- ignora las búsquedas de ese proveedor en runtime (sin cascada),
-- los usuarios afectados ven el estado y reciben notificación
-- PROVIDER_DISABLED. status_reason (texto libre) explica el porqué.
-- Reactivación: solo ADMIN.
-- provider_type distingue la estrategia de extracción:
--   SCRAPER → se scrapea HTML (ej. TuBoleta con Jsoup)
--   API     → se consume una API oficial (con auth, etc.)
-- config (JSONB) guarda la configuración flexible y específica de
-- cada proveedor (user-agent, selectores CSS, endpoints, headers,
-- credenciales-referencia, rate-limit...), evitando el problema de
-- v1 donde todo vivía disperso y hardcodeado en YAML.
-- ============================================================
CREATE TABLE providers (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    provider_type VARCHAR(20)   NOT NULL DEFAULT 'SCRAPER'
                  CHECK (provider_type IN ('SCRAPER', 'API')),
    base_url      VARCHAR(500)  NOT NULL,
    search_url    VARCHAR(500),
    config             JSONB,
    status             VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
                       CHECK (status IN ('ACTIVE', 'DISABLED')),
    status_reason      TEXT,
    status_changed_at  TIMESTAMPTZ,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ
);

INSERT INTO providers (name, provider_type, base_url, search_url, config) VALUES (
    'TuBoleta',
    'SCRAPER',
    'https://www.tuboleta.com',
    'https://www.tuboleta.com/es/callback/search-results?s={term}',
    '{"user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36", "timeout_ms": 15000, "rate_limit_ms": 2000}'::jsonb
);


-- ============================================================
-- NOTIF_CHANNELS — Catálogo de canales de notificación
-- ============================================================
-- Solo EMAIL activo en V2.
-- TELEGRAM y WHATSAPP: deuda técnica consciente (filas listas,
-- inactivas, para habilitarlas sin cambio de esquema).
-- ============================================================
CREATE TABLE notif_channels (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(50)   NOT NULL UNIQUE,
    is_active   BOOLEAN       NOT NULL DEFAULT FALSE
);

INSERT INTO notif_channels (name, is_active) VALUES
    ('EMAIL',     TRUE),
    ('TELEGRAM',  FALSE),
    ('WHATSAPP',  FALSE);


-- ============================================================
-- USERS
-- ============================================================
-- Esquema multiusuario desde el inicio.
-- role: ADMIN (gestión interna / panel Vue) o USER (autoservicio).
--   Conviven en la misma tabla en vez de dos sistemas separados.
-- status: INACTIVE → el usuario no puede iniciar sesión y el
--   scheduler salta sus búsquedas (desactivación por ADMIN).
-- password_hash almacena el hash BCrypt de Spring Security.
-- (Verificación de email / recuperación de contraseña se
--  modelarán en una migración futura, no ahora.)
-- ============================================================
CREATE TABLE users (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email           VARCHAR(255)  NOT NULL UNIQUE,
    name            VARCHAR(100)  NOT NULL,
    password_hash   VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL DEFAULT 'USER'
                    CHECK (role IN ('ADMIN', 'USER')),
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);


-- ============================================================
-- USER_NOTIFICATION_CHANNELS — Destinos de notificación por usuario
-- ============================================================
-- Un destino reutilizable (canal + dirección) que el usuario
-- define UNA vez y puede asociar a varias búsquedas.
-- Reemplaza el 'destination' repetido de v1 (que iba atado a cada
-- notificación) y el destinatario único hardcodeado.
-- destination = email / chat_id de Telegram / teléfono, según canal.
-- ============================================================
CREATE TABLE user_notification_channels (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT        NOT NULL REFERENCES users(id),
    channel_id    BIGINT        NOT NULL REFERENCES notif_channels(id),
    destination   VARCHAR(255)  NOT NULL,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_channel_destination UNIQUE (user_id, channel_id, destination)
);

CREATE INDEX idx_user_notif_channels_user ON user_notification_channels(user_id);


-- ============================================================
-- SEARCHES — Búsquedas configuradas por usuario
-- ============================================================
-- Una búsqueda es un término del usuario (ej. "Bad Bunny").
-- El proveedor YA NO vive aquí: una misma búsqueda puede
-- monitorearse en varios proveedores a la vez (ver search_providers).
-- term            = tal como lo escribió el usuario.
-- term_normalized = trim + minúsculas + colapso de espacios
--   (REQ-BUS-002); unicidad por usuario sobre el normalizado,
--   excluyendo búsquedas DELETED (índice único parcial).
-- check_frequency_hours = cada cuánto se monitorea, elegido por
--   el usuario entre presets cerrados (REQ-BUS-005).
-- status = DELETED → eliminación lógica (nunca DELETE físico).
-- status = INACTIVE → el scheduler la salta.
-- ============================================================
CREATE TABLE searches (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id               BIGINT        NOT NULL REFERENCES users(id),
    term                  VARCHAR(500)  NOT NULL,
    term_normalized       VARCHAR(500)  NOT NULL,
    check_frequency_hours INT           NOT NULL DEFAULT 24
                          CHECK (check_frequency_hours IN (6, 12, 24, 48)),
    status                VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
                          CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_searches_user_id ON searches(user_id);
CREATE INDEX idx_searches_status  ON searches(status);
CREATE UNIQUE INDEX uq_searches_user_term
    ON searches(user_id, term_normalized)
    WHERE status <> 'DELETED';


-- ============================================================
-- SEARCH_PROVIDERS — Relación N:M búsqueda ↔ proveedor
-- ============================================================
-- Cada par (búsqueda, proveedor) se ejecuta y se pausa de forma
-- independiente. El scheduler también verifica provider.status
-- en runtime.
-- last_run_at = última corrida de ESTE par (antes en searches.last_run_at).
-- ============================================================
CREATE TABLE search_providers (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    search_id       BIGINT        NOT NULL REFERENCES searches(id),
    provider_id     BIGINT        NOT NULL REFERENCES providers(id),
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    last_run_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_search_provider UNIQUE (search_id, provider_id)
);

CREATE INDEX idx_search_providers_search   ON search_providers(search_id);
CREATE INDEX idx_search_providers_provider ON search_providers(provider_id);
CREATE INDEX idx_search_providers_active   ON search_providers(is_active);


-- ============================================================
-- PROVIDER_RUNS — Log de corridas del scheduler (REQ-DET-005)
-- ============================================================
-- Una fila por petición real al proveedor. El scheduler agrupa
-- los pares vencidos por (provider, term_normalized) → UNA sola
-- petición HTTP por grupo, aplicada a todos los pares suscritos.
-- success = la petición Y la extracción de TODOS los ítems
--   terminaron sin error. Una corrida con ítems fallidos se marca
--   success = FALSE: sus eventos extraídos SÍ se procesan
--   (NEW/CHANGED), pero NO incrementa miss_count ni genera
--   REMOVED (regla anti falso-eliminado de REQ-DET-003).
-- Observabilidad: "qué corrió, cuándo, cómo terminó".
-- ============================================================
CREATE TABLE provider_runs (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider_id      BIGINT        NOT NULL REFERENCES providers(id),
    term_normalized  VARCHAR(500)  NOT NULL,
    started_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    finished_at      TIMESTAMPTZ,
    success          BOOLEAN,
    error_message    TEXT,
    events_found     INT,
    pairs_applied    INT
);

CREATE INDEX idx_provider_runs_provider ON provider_runs(provider_id, started_at DESC);


-- ============================================================
-- SEARCH_NOTIFICATIONS — Qué destinos se notifican por búsqueda
-- ============================================================
-- Fan-out: una búsqueda puede notificar a varios destinos.
-- Referencia a user_notification_channels (no guarda el destino
-- directo), para reutilizar destinos entre búsquedas.
-- ============================================================
CREATE TABLE search_notifications (
    id                             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    search_id                      BIGINT      NOT NULL REFERENCES searches(id),
    user_notification_channel_id   BIGINT      NOT NULL REFERENCES user_notification_channels(id),
    is_active                      BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_search_notif_destination UNIQUE (search_id, user_notification_channel_id)
);

CREATE INDEX idx_search_notifs_search ON search_notifications(search_id);


-- ============================================================
-- EVENTS — Eventos detectados por el scheduler
-- ============================================================
-- Cuelga de search_providers: el mismo evento hallado en dos
-- proveedores distintos son DOS filas (cada fuente es independiente).
-- Histórico INMUTABLE: nunca se borra físicamente.
-- status = REMOVED → el evento dejó de aparecer en la fuente.
-- external_id = identificador estable del ítem en la fuente
--   (ej. el "link" de la API de TuBoleta: /es/eventos/j-balvin-...).
--   Más robusto que el showUniqueId de v1 (título+venue+ciudad),
--   que rompía si el show se renombraba o cambiaba de venue.
-- event_date_raw = texto original ("11 Abril", "21 Marzo - 9 Mayo").
-- event_date = parseado cuando sea posible, nullable.
-- raw_json = payload JSON completo del ítem de la fuente.
-- Detección de cambios (REQ-DET-001): diff campo a campo contra
--   lo almacenado — SIN hash (era redundante y solo cubría 3 campos).
-- miss_count (REQ-DET-003): corridas EXITOSAS consecutivas del par
--   sin ver el evento; a las 2 → REMOVED + notificación. Una corrida
--   fallida nunca lo incrementa. Se resetea a 0 si reaparece.
-- ============================================================
CREATE TABLE events (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    search_provider_id  BIGINT        NOT NULL REFERENCES search_providers(id),
    external_id         VARCHAR(500)  NOT NULL,
    title               VARCHAR(500),
    venue               VARCHAR(500),
    event_date_raw      VARCHAR(100),
    event_date          DATE,
    raw_json            JSONB,
    status              VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'REMOVED')),
    miss_count          INT           NOT NULL DEFAULT 0,
    first_seen_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    last_seen_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_event_per_search_provider UNIQUE (search_provider_id, external_id)
);

CREATE INDEX idx_events_search_provider ON events(search_provider_id);
CREATE INDEX idx_events_status          ON events(status);


-- ============================================================
-- EVENT_CHANGES — Histórico de cambios campo a campo
-- ============================================================
-- Una fila por cada campo EXTRAÍDO que cambió en un evento
-- (REQ-DET-002: cualquier campo extraído que cambie alerta;
-- varios campos en una corrida = varias filas aquí pero UNA
-- sola notificación CHANGED).
-- Reemplaza el 'resumenCambios' de texto libre de v1 por un log
-- estructurado y consultable.
-- ============================================================
CREATE TABLE event_changes (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id        BIGINT        NOT NULL REFERENCES events(id),
    field_name      VARCHAR(100)  NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    detected_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_event_changes_event ON event_changes(event_id);


-- ============================================================
-- NOTIFICATIONS — El hecho notificable (inbox del usuario)
-- ============================================================
-- Modelo de dos niveles (REQ-NOT-003): esta tabla es el HECHO
-- ("hay un show nuevo para tu búsqueda X"); las entregas por
-- canal viven en notifications_log. Una notificación puede
-- existir sin ninguna entrega (solo inbox web).
-- read_at → estado leído/no leído del inbox in-app; también
--   alimenta el destaque de "nuevo/cambiado" en el dashboard.
--   Se marca leída de forma individual (o "marcar todas"), nunca
--   automáticamente por visitar el inbox.
-- type: NEW = evento nuevo (o reaparecido tras REMOVED),
--       CHANGED = cambio detectado (UNA notificación por evento
--       por corrida, aunque cambien varios campos),
--       REMOVED = evento desapareció de la fuente,
--       PROVIDER_DISABLED = fuente deshabilitada por ADMIN
--       (REQ-FUE-002; sin evento → event_id NULL, una por
--       búsqueda afectada).
-- ============================================================
CREATE TABLE notifications (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users(id),
    search_id       BIGINT        NOT NULL REFERENCES searches(id),
    event_id        BIGINT        REFERENCES events(id),
    type            VARCHAR(20)   NOT NULL
                    CHECK (type IN ('NEW', 'CHANGED', 'REMOVED', 'PROVIDER_DISABLED')),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user   ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id) WHERE read_at IS NULL;
CREATE INDEX idx_notifications_search ON notifications(search_id);


-- ============================================================
-- NOTIFICATIONS_LOG — Log de entregas por canal
-- ============================================================
-- Auditoría inmutable de envíos: una fila por cada intento de
-- entrega de una notificación por un canal.
-- destination se guarda como SNAPSHOT (texto): aunque el destino
-- original en user_notification_channels cambie o se elimine, el
-- log conserva a dónde se envió realmente.
-- ============================================================
CREATE TABLE notifications_log (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    notification_id BIGINT        NOT NULL REFERENCES notifications(id),
    channel_id      BIGINT        NOT NULL REFERENCES notif_channels(id),
    destination     VARCHAR(255)  NOT NULL,
    sent_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    success         BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_notif_log_notification ON notifications_log(notification_id);
