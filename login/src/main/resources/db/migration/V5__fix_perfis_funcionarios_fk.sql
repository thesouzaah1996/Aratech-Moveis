ALTER TABLE perfis_funcionarios DROP CONSTRAINT perfis_funcionarios_funcionario_id_fkey;

ALTER TABLE perfis_funcionarios
    ADD CONSTRAINT perfis_funcionarios_funcionario_id_fkey
    FOREIGN KEY (funcionario_id) REFERENCES funcionario(id);
