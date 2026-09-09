CREATE TABLE mecanicos (
    id     BIGSERIAL     PRIMARY KEY,
    nome   VARCHAR(150)  NOT NULL,
    ativo  BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_mecanico_ativo ON mecanicos (ativo);
