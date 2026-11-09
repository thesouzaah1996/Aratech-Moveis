CREATE TABLE IF NOT EXISTS acesso_conta (
    funcionario_id BIGINT PRIMARY KEY REFERENCES funcionario(id),
    senha_hash VARCHAR(256) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    administrador BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- Conta técnica inicial, independente dos dados pessoais de funcionários do RH.
ALTER TABLE acesso_conta ADD COLUMN IF NOT EXISTS trocar_senha BOOLEAN NOT NULL DEFAULT FALSE;
CREATE TABLE IF NOT EXISTS acesso_administrador (
    id BIGINT PRIMARY KEY CHECK (id = 0),
    login VARCHAR(64) NOT NULL UNIQUE,
    senha_hash VARCHAR(256) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    trocar_senha BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS acesso_permissao (
    codigo VARCHAR(100) PRIMARY KEY
);
CREATE TABLE IF NOT EXISTS perfil_permissao_acesso (
    perfil_id BIGINT NOT NULL REFERENCES perfil_acesso(id),
    permissao VARCHAR(100) NOT NULL REFERENCES acesso_permissao(codigo),
    PRIMARY KEY (perfil_id, permissao)
);
CREATE TABLE IF NOT EXISTS acesso_tentativa (
    chave VARCHAR(80) PRIMARY KEY,
    janela TIMESTAMPTZ NOT NULL,
    contagem INTEGER NOT NULL
);
CREATE OR REPLACE FUNCTION revogar_sessoes_funcionario() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.ativo IS DISTINCT FROM NEW.ativo OR lower(OLD.email) IS DISTINCT FROM lower(NEW.email) THEN
        UPDATE acesso_conta SET versao=versao+1 WHERE funcionario_id=NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE TRIGGER funcionario_revogar_sessoes
AFTER UPDATE OF ativo, email ON funcionario
FOR EACH ROW EXECUTE FUNCTION revogar_sessoes_funcionario();

-- Remove os tokens do fluxo de recuperacao descontinuado.
DROP TABLE IF EXISTS acesso_token_senha;

ALTER TABLE acesso_conta ADD COLUMN IF NOT EXISTS email_corporativo VARCHAR(150);
CREATE UNIQUE INDEX IF NOT EXISTS uk_acesso_email_corporativo ON acesso_conta(lower(email_corporativo)) WHERE email_corporativo IS NOT NULL;
