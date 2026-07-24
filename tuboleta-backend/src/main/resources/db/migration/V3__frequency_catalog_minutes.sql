-- V3 — Frecuencia de monitoreo en MINUTOS + catálogo gestionable.
--
-- Antes: searches.check_frequency_hours (horas, CHECK IN 6/12/24/48).
-- Ahora: un catálogo de frecuencias permitidas (en minutos, gestionable por el
-- admin) y searches.check_frequency_minutes con el valor elegido. El scheduler
-- pasa a razonar en minutos, permitiendo intervalos cortos (ej. cada 10 min).
--
-- Migración incremental (no se edita V1) para no romper el checksum de Flyway
-- ni recrear la BD con datos.

-- 1) Catálogo de frecuencias permitidas (el admin lo gestiona; el usuario elige)
CREATE TABLE frequencies (
    id         BIGSERIAL PRIMARY KEY,
    label      VARCHAR(60) NOT NULL,
    minutes    INT         NOT NULL CHECK (minutes > 0),
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_frequencies_minutes UNIQUE (minutes)
);

INSERT INTO frequencies (label, minutes) VALUES
    ('Cada 5 minutos', 5),
    ('Cada 10 minutos', 10),
    ('Cada 30 minutos', 30),
    ('Cada hora', 60),
    ('Cada 6 horas', 360),
    ('Cada 12 horas', 720),
    ('Cada día', 1440),
    ('Cada 2 días', 2880);

-- 2) Nueva columna en minutos, migrando los valores existentes (horas * 60)
ALTER TABLE searches ADD COLUMN check_frequency_minutes INT;
UPDATE searches SET check_frequency_minutes = check_frequency_hours * 60;
ALTER TABLE searches ALTER COLUMN check_frequency_minutes SET NOT NULL;
ALTER TABLE searches ALTER COLUMN check_frequency_minutes SET DEFAULT 1440;

-- 3) Quitar la columna vieja de horas y su CHECK
ALTER TABLE searches DROP CONSTRAINT IF EXISTS searches_check_frequency_hours_check;
ALTER TABLE searches DROP COLUMN check_frequency_hours;
