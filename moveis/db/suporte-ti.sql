CREATE SEQUENCE IF NOT EXISTS suporte_ti_chamado_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS suporte_ti_chamado (
 id BIGINT PRIMARY KEY DEFAULT nextval('suporte_ti_chamado_seq'),
 numero VARCHAR(40) NOT NULL UNIQUE,
 tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ACESSO','HARDWARE','SOFTWARE','REDE','IMPRESSORA','COMUNICACAO','OUTROS')),
 prioridade VARCHAR(10) NOT NULL CHECK (prioridade IN ('BAIXA','MEDIA','ALTA','CRITICA')),
 modulo_afetado VARCHAR(100) NOT NULL DEFAULT '',
 titulo VARCHAR(150) NOT NULL CHECK (char_length(btrim(titulo)) BETWEEN 3 AND 150),
 solicitante VARCHAR(150) NOT NULL CHECK (char_length(btrim(solicitante)) BETWEEN 3 AND 150),
 setor VARCHAR(100) NOT NULL CHECK (char_length(btrim(setor)) BETWEEN 2 AND 100),
 contato VARCHAR(11) NOT NULL DEFAULT '' CHECK (contato='' OR contato ~ '^([0-9]{2,6}|[1-9][0-9]{9,10})$'),
 descricao VARCHAR(3000) NOT NULL CHECK (char_length(btrim(descricao)) BETWEEN 3 AND 3000),
 tentativas_solucao VARCHAR(2000) NOT NULL DEFAULT '',
 status VARCHAR(20) NOT NULL DEFAULT 'ABERTO' CHECK (status IN ('ABERTO','EM_ATENDIMENTO','CONCLUIDO','CANCELADO')),
 data_abertura TIMESTAMPTZ NOT NULL,
 chave_operacao UUID NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS ix_suporte_ti_acompanhamento ON suporte_ti_chamado(status,prioridade,data_abertura,id);
