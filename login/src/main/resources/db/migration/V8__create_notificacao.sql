CREATE TABLE notificacao (
    id                BIGSERIAL    PRIMARY KEY,
    assunto           VARCHAR(255),
    destinatario      VARCHAR(255),
    mensagem          TEXT,
    tipo_notificacao  VARCHAR(30),
    funcionario_id    BIGINT       REFERENCES funcionario(id),
    data_criacao      TIMESTAMP
);
