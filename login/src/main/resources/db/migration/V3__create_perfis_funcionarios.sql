CREATE TABLE perfis_funcionarios (
    funcionario_id  BIGINT NOT NULL REFERENCES funcionario(id),
    perfil_id       BIGINT NOT NULL REFERENCES perfis(id),
    PRIMARY KEY (funcionario_id, perfil_id)
);
