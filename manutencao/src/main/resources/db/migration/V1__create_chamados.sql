CREATE TABLE chamados (
    id             BIGSERIAL     PRIMARY KEY,
    equipamento    VARCHAR(150)  NOT NULL,
    tipo           VARCHAR(20)   NOT NULL,
    prioridade     VARCHAR(10)   NOT NULL,
    solicitante    VARCHAR(150)  NOT NULL,
    setor          VARCHAR(100)  NOT NULL,
    telefone       VARCHAR(16),
    descricao      VARCHAR(1000) NOT NULL,
    mecanico       VARCHAR(150),
    status         VARCHAR(20)   NOT NULL,
    data_abertura  TIMESTAMP     NOT NULL
);
