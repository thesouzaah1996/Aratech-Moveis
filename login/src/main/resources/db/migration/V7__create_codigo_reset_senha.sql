CREATE TABLE codigo_reset_senha (
    id               BIGSERIAL    PRIMARY KEY,
    codigo           VARCHAR(255) NOT NULL UNIQUE,
    usado            BOOLEAN      NOT NULL DEFAULT FALSE,
    data_expiracao   TIMESTAMP    NOT NULL,
    id_funcionario   BIGINT       NOT NULL REFERENCES funcionario(id)
);
