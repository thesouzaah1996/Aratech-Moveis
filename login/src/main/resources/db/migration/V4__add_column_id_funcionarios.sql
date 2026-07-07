ALTER TABLE funcionario RENAME COLUMN id TO id_funcionario;
ALTER TABLE funcionario ADD COLUMN id BIGSERIAL;

ALTER TABLE perfis_funcionarios DROP CONSTRAINT perfis_funcionarios_funcionario_id_fkey;

ALTER TABLE funcionario DROP CONSTRAINT funcionario_pkey;
ALTER TABLE funcionario ALTER COLUMN id_funcionario SET NOT NULL;
ALTER TABLE funcionario ADD CONSTRAINT funcionario_id_funcionario_key UNIQUE (id_funcionario);
ALTER TABLE funcionario ADD PRIMARY KEY (id);

ALTER TABLE perfis_funcionarios
    ADD CONSTRAINT perfis_funcionarios_funcionario_id_fkey
    FOREIGN KEY (funcionario_id) REFERENCES funcionario(id_funcionario);
