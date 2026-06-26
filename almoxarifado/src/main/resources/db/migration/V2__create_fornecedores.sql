CREATE TABLE fornecedores (
    id                     BIGSERIAL    PRIMARY KEY,
    nome                   VARCHAR(100) NOT NULL,
    email                  VARCHAR(150) NOT NULL UNIQUE,
    telefone               VARCHAR(255) NOT NULL,
    ativo                  BOOLEAN      NOT NULL DEFAULT TRUE,
    nome_representante     VARCHAR(255),
    telefone_representante VARCHAR(255),
    email_representante    VARCHAR(255)
);
