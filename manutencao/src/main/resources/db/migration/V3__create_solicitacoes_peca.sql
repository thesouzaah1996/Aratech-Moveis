CREATE TABLE solicitacoes_peca (
    id           BIGSERIAL     PRIMARY KEY,
    nome_peca    VARCHAR(150)  NOT NULL,
    codigo       VARCHAR(50),
    quantidade   INTEGER       NOT NULL CHECK (quantidade > 0),
    unidade      VARCHAR(10)   NOT NULL,
    equipamento  VARCHAR(150)  NOT NULL,
    finalidade   VARCHAR(20)   NOT NULL,
    prioridade   VARCHAR(10)   NOT NULL,
    solicitante  VARCHAR(150)  NOT NULL,
    setor        VARCHAR(100)  NOT NULL,
    telefone     VARCHAR(16),
    observacoes  VARCHAR(1000) NOT NULL,
    criado_em    TIMESTAMP     NOT NULL
);
