CREATE TABLE recebimentos (
    id                 BIGSERIAL     PRIMARY KEY,
    nota_fiscal        VARCHAR(50)   NOT NULL UNIQUE,
    empresa            VARCHAR(100)  NOT NULL,
    nome_motorista     VARCHAR(150)  NOT NULL,
    placa              VARCHAR(10)   NOT NULL,
    descricao_carga    VARCHAR(255)  NOT NULL,
    status_recebimento VARCHAR(20)   NOT NULL,
    setor_responsavel  VARCHAR(255)  NOT NULL
);
