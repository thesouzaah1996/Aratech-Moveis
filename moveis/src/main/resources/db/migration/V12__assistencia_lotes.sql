CREATE TABLE IF NOT EXISTS assistencia_lote (
              id BIGSERIAL PRIMARY KEY, origem_id BIGINT NOT NULL UNIQUE,
              codigo VARCHAR(255) NOT NULL, produto VARCHAR(255) NOT NULL, empresa VARCHAR(255),
              quantidade INTEGER NOT NULL CHECK (quantidade > 0), data_entrada DATE NOT NULL,
              responsavel VARCHAR(255), defeito VARCHAR(255), status VARCHAR(30) NOT NULL,
              iniciado_em TIMESTAMPTZ, finalizado_em TIMESTAMPTZ, carregado_em TIMESTAMPTZ,
              versao BIGINT NOT NULL DEFAULT 0
            );
