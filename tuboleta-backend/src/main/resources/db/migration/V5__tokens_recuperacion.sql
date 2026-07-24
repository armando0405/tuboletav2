-- Tokens de recuperación de contraseña (Fase 1 - cuenta de usuario).
-- Se guarda el HASH del token (SHA-256), nunca el token en claro: si la BD se
-- filtra, los tokens no son utilizables. Expira en 1h; usado_en marca consumo.
CREATE TABLE tokens_recuperacion (
    id            BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id    BIGINT        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token_hash    VARCHAR(255)  NOT NULL UNIQUE,
    expira_en     TIMESTAMPTZ   NOT NULL,
    usado_en      TIMESTAMPTZ,
    creado_en     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tokens_recuperacion_usuario ON tokens_recuperacion (usuario_id);
