CREATE TABLE registro_chegada (
    id               BIGSERIAL    PRIMARY KEY,
    nota_fiscal      VARCHAR(50)  NOT NULL UNIQUE,
    empresa          VARCHAR(100) NOT NULL,
    nome_motorista   VARCHAR(150) NOT NULL,
    placa            VARCHAR(10)  NOT NULL,
    setor_responsavel VARCHAR(20) NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    data_entrada     TIMESTAMP    NOT NULL
);
