CREATE TABLE perfis_funcionarios (
    funcionario_id BIGINT NOT NULL REFERENCES funcionarios(id),
    perfil_id      BIGINT NOT NULL REFERENCES perfis(id),
    PRIMARY KEY (funcionario_id, perfil_id)
);
