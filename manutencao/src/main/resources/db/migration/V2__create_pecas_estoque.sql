CREATE TABLE pecas_estoque (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    codigo      VARCHAR(50)  NOT NULL UNIQUE,
    quantidade  INTEGER      NOT NULL CHECK (quantidade >= 0),
    unidade     VARCHAR(10)  NOT NULL,
    localizacao VARCHAR(100) NOT NULL,
    descricao   VARCHAR(500)
);
