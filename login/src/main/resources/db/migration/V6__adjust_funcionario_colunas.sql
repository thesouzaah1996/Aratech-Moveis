ALTER TABLE funcionario RENAME COLUMN nome TO nome_funcionario;

ALTER TABLE funcionario ADD COLUMN email_pessoal VARCHAR(255);
ALTER TABLE funcionario ADD COLUMN senha VARCHAR(255);
ALTER TABLE funcionario ADD COLUMN primeiro_login BOOLEAN NOT NULL DEFAULT TRUE;
