CREATE TABLE rh_ponto_escala (
    funcionario_id BIGINT NOT NULL REFERENCES funcionario(id),
    dia_semana INTEGER NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    horarios VARCHAR(35) NOT NULL,
    extra_minutos INTEGER NOT NULL DEFAULT 0 CHECK (extra_minutos BETWEEN 0 AND 120),
    PRIMARY KEY (funcionario_id, dia_semana)
);
INSERT INTO perfil_acesso(nome) VALUES ('#') ON CONFLICT DO NOTHING;
INSERT INTO perfil_permissao_acesso(perfil_id, permissao)
SELECT id, p FROM perfil_acesso CROSS JOIN
    (VALUES ('RH_PONTO.CONSULTAR'), ('RH_PONTO.ALTERAR')) AS permissoes(p)
WHERE nome = '#' ON CONFLICT DO NOTHING;
