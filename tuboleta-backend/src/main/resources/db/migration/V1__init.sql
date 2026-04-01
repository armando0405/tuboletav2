-- ============================================================
-- TU_BOLETA V2 — Migración inicial
-- Flyway: V1__init.sql
-- Fecha: 2026-03
-- ============================================================
-- REGLA CRÍTICA: Este archivo NO debe modificarse una vez
-- ejecutado en cualquier entorno. Cambios posteriores
-- van en V2__xxx.sql
-- ============================================================


-- ============================================================
-- PROVIDERS — Catálogo de plataformas de boletas
-- ============================================================
-- is_active = false → el scheduler ignora las búsquedas
-- de ese proveedor en tiempo de ejecución (sin cascada)
-- ============================================================
CREATE TABLE providers (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    base_url    VARCHAR(500)    NOT NULL,
    search_url  VARCHAR(500)    NOT NULL,
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

INSERT INTO providers (name, base_url, search_url) VALUES (
    'TuBoleta',
    'https://www.tuboleta.com',
    'https://www.tuboleta.com/es/callback/search-results?s={term}'
);


-- ============================================================
-- NOTIF_CHANNELS — Catálogo de canales de notificación
-- ============================================================
-- Solo EMAIL activo en V2.
-- TELEGRAM y WHATSAPP: deuda técnica consciente.
-- ============================================================
CREATE TABLE notif_channels (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL UNIQUE,
    is_active   BOOLEAN         NOT NULL DEFAULT FALSE
);

INSERT INTO notif_channels (name, is_active) VALUES
    ('EMAIL',     TRUE),
    ('TELEGRAM',  FALSE),
    ('WHATSAPP',  FALSE);


-- ============================================================
-- USERS
-- ============================================================
-- Esquema multiusuario anticipado desde el inicio.
-- password_hash almacena el hash BCrypt de Spring Security.
-- ============================================================
CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    name            VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);


-- ============================================================
-- SEARCHES — Búsquedas configuradas por usuario
-- ============================================================
-- status = DELETED → eliminación lógica (nunca DELETE físico)
-- status = INACTIVE → el scheduler la salta
-- El scheduler también verifica provider.is_active en runtime
-- ============================================================
CREATE TABLE searches (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    provider_id     BIGINT          NOT NULL REFERENCES providers(id),
    name            VARCHAR(200)    NOT NULL,
    term            VARCHAR(500)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    last_run_at     TIMESTAMP
);

CREATE INDEX idx_searches_user_id    ON searches(user_id);
CREATE INDEX idx_searches_status     ON searches(status);
CREATE INDEX idx_searches_provider   ON searches(provider_id);


-- ============================================================
-- SEARCH_NOTIFICATIONS — Destinos de notificación por búsqueda
-- ============================================================
-- Una búsqueda puede tener múltiples destinos (emails).
-- channel_id preparado para futuros canales sin cambio de esquema.
-- ============================================================
CREATE TABLE search_notifications (
    id              BIGSERIAL       PRIMARY KEY,
    search_id       BIGINT          NOT NULL REFERENCES searches(id),
    channel_id      BIGINT          NOT NULL REFERENCES notif_channels(id),
    destination     VARCHAR(255)    NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_search_notifs_search ON search_notifications(search_id);


-- ============================================================
-- EVENTS — Eventos detectados por el scheduler
-- ============================================================
-- Histórico INMUTABLE: nunca se borran físicamente.
-- status = REMOVED → el evento dejó de aparecer en la fuente.
-- external_id = campo "link" de la API de TuBoleta.
--   Ej: /es/eventos/j-balvin-made-cucuta
-- event_date_raw = texto original ("11 Abril", "21 Marzo - 9 Mayo")
-- event_date = parseado cuando sea posible, nullable.
-- raw_json = payload JSON completo del ítem de la API.
-- hash = SHA-256 de (title + venue + event_date_raw).
-- ============================================================
CREATE TABLE events (
    id              BIGSERIAL       PRIMARY KEY,
    search_id       BIGINT          NOT NULL REFERENCES searches(id),
    external_id     VARCHAR(500)    NOT NULL,
    title           VARCHAR(500),
    venue           VARCHAR(500),
    event_date_raw  VARCHAR(100),
    event_date      DATE,
    hash            VARCHAR(64)     NOT NULL,
    raw_json        JSONB,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'REMOVED')),
    first_seen_at   TIMESTAMP       NOT NULL DEFAULT NOW(),
    last_seen_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_event_per_search UNIQUE (search_id, external_id)
);

CREATE INDEX idx_events_search_id    ON events(search_id);
CREATE INDEX idx_events_status       ON events(status);
CREATE INDEX idx_events_hash         ON events(hash);


-- ============================================================
-- EVENT_CHANGES — Histórico de cambios campo a campo
-- ============================================================
-- Una fila por cada campo que cambió en un evento.
-- field_name valores esperados: 'title', 'venue', 'event_date_raw'
-- ============================================================
CREATE TABLE event_changes (
    id              BIGSERIAL       PRIMARY KEY,
    event_id        BIGINT          NOT NULL REFERENCES events(id),
    field_name      VARCHAR(100)    NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    detected_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_event_changes_event ON event_changes(event_id);


-- ============================================================
-- NOTIFICATIONS_LOG — Registro de notificaciones enviadas
-- ============================================================
-- Auditoría completa: qué se notificó, cuándo, a quién,
-- por qué canal y si fue exitoso.
-- type: NEW = evento nuevo, CHANGED = cambio detectado,
--       REMOVED = evento desapareció de la fuente
-- ============================================================
CREATE TABLE notifications_log (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    search_id       BIGINT          NOT NULL REFERENCES searches(id),
    event_id        BIGINT          REFERENCES events(id),
    type            VARCHAR(20)     NOT NULL
                    CHECK (type IN ('NEW', 'CHANGED', 'REMOVED')),
    channel_id      BIGINT          NOT NULL REFERENCES notif_channels(id),
    destination     VARCHAR(255)    NOT NULL,
    sent_at         TIMESTAMP       NOT NULL DEFAULT NOW(),
    success         BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_notif_log_user      ON notifications_log(user_id);
CREATE INDEX idx_notif_log_search    ON notifications_log(search_id);
