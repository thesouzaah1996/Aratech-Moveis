CREATE TABLE produtos (
    id                  BIGSERIAL    PRIMARY KEY,
    nome                VARCHAR(255) NOT NULL,
    sku                 VARCHAR(255) NOT NULL UNIQUE,
    quantidade          INTEGER      CHECK (quantidade >= 0),
    local_armazenamento VARCHAR(255) NOT NULL,
    descricao           VARCHAR(255),
    vencimento_produto  TIMESTAMP,
    criado_em           TIMESTAMP,
    categoria_id        BIGINT       REFERENCES categorias(id),
    fornecedor_id       BIGINT       REFERENCES fornecedores(id)
);
