package com.aratech.moveis.security.repository.imp;

import com.aratech.moveis.security.repository.ProvisionarAdministradorTemporarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProvisionarAdministradorTemporarioRepositoryImp
        implements ProvisionarAdministradorTemporarioRepository {
    private final JdbcTemplate jdbc;

    public void bloquearAdministradorParaBootstrap() {
        jdbc.execute("LOCK TABLE acesso_administrador IN SHARE ROW EXCLUSIVE MODE");
    }

    public Boolean existeAdministrador() {
        return jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM acesso_administrador)", Boolean.class);
    }

    public int inserirAdministrador(String loginValidado, String encode) {
        return jdbc.update(
                "INSERT INTO acesso_administrador(id,login,senha_hash,trocar_senha) VALUES"
                        + " (0,?,?,TRUE)",
                loginValidado,
                encode);
    }
}
