ALTER TABLE mecanicos ADD COLUMN turno VARCHAR(10) NOT NULL DEFAULT 'MANHA';
ALTER TABLE mecanicos ALTER COLUMN turno DROP DEFAULT;

CREATE TABLE mecanico_especialidades (
    mecanico_id   BIGINT      NOT NULL REFERENCES mecanicos(id),
    especialidade VARCHAR(30) NOT NULL,
    PRIMARY KEY (mecanico_id, especialidade)
);
