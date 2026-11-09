package com.aratech.moveis.security.repository.imp;

import com.aratech.moveis.security.repository.BootstrapAdministradorRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BootstrapAdministradorRepositoryImp implements BootstrapAdministradorRepository {
    private final JdbcTemplate jdbc;

    public void bloquearContasParaBootstrap() {
        jdbc.execute("LOCK TABLE acesso_conta IN SHARE ROW EXCLUSIVE MODE");
    }

    public Boolean administradorConfigurado() {
        return jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM acesso_conta WHERE administrador IS TRUE) OR"
                        + " EXISTS(SELECT 1 FROM acesso_administrador)",
                Boolean.class);
    }

    public List<String> buscarLoginDoFuncionarioAtivoComBloqueio(Long usuarioId) {
        return jdbc.queryForList(
                "SELECT COALESCE(c.email_corporativo,f.email) FROM funcionario f LEFT JOIN"
                    + " acesso_conta c ON c.funcionario_id=f.id WHERE f.id=? AND f.ativo IS TRUE"
                    + " FOR UPDATE OF f",
                String.class,
                usuarioId);
    }

    public List<Long> bloquearFuncionario(Long usuarioId) {
        return jdbc.queryForList(
                "SELECT id FROM funcionario WHERE id=? AND ativo IS TRUE FOR UPDATE",
                Long.class,
                usuarioId);
    }

    public int inserirConta(Long usuarioId, String encode) {
        return jdbc.update(
                "INSERT INTO acesso_conta(funcionario_id, senha_hash, administrador) VALUES (?, ?,"
                        + " TRUE) ON CONFLICT (funcionario_id) DO UPDATE SET"
                        + " senha_hash=EXCLUDED.senha_hash, administrador=TRUE,"
                        + " versao=acesso_conta.versao+1",
                usuarioId,
                encode);
    }
}
