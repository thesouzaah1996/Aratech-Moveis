CREATE TABLE funcionario (
    id                 BIGSERIAL    PRIMARY KEY,
    nome               VARCHAR(255),
    email_corporativo  VARCHAR(255),
    ativo              BOOLEAN      NOT NULL DEFAULT TRUE
);
