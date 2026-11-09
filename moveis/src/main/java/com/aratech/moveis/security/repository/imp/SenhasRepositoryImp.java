package com.aratech.moveis.security.repository.imp;

import com.aratech.moveis.security.repository.SenhasRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SenhasRepositoryImp implements SenhasRepository {
    private final JdbcTemplate jdbc;

    public List<String> buscarEmailDoFuncionarioAtivoComBloqueio(Long id) {
        return jdbc.queryForList(
                "SELECT email FROM funcionario WHERE id=? AND ativo IS TRUE FOR UPDATE",
                String.class,
                id);
    }

    public Boolean existeConta(Long id) {
        return jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM acesso_conta WHERE funcionario_id=?)",
                Boolean.class,
                id);
    }

    public Object bloquearIdentificadorLogin(String login) {
        return jdbc.queryForObject(
                "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", Object.class, login);
    }

    public Boolean loginOcupado(String login, Long id, String login2) {
        return jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM acesso_conta c JOIN funcionario f ON"
                    + " f.id=c.funcionario_id WHERE lower(COALESCE(c.email_corporativo, f.email))=?"
                    + " UNION ALL SELECT 1 FROM funcionario WHERE id<>? AND lower(email)=?)",
                Boolean.class,
                login,
                id,
                login2);
    }

    public int inserirConta(Long id, String encode, String corporativo) {
        return jdbc.update(
                "INSERT INTO acesso_conta(funcionario_id, senha_hash, trocar_senha,"
                        + " email_corporativo) VALUES (?, ?, TRUE, ?)",
                id,
                encode,
                corporativo);
    }

    public List<String> buscarHashDoAdministradorComBloqueio() {
        return jdbc.queryForList(
                "SELECT senha_hash FROM acesso_administrador WHERE id=0 AND ativo IS TRUE FOR"
                        + " UPDATE",
                String.class);
    }

    public int alterarSenhaDoAdministrador(String encode) {
        return jdbc.update(
                "UPDATE acesso_administrador SET senha_hash=?, trocar_senha=FALSE, versao=versao+1"
                        + " WHERE id=0",
                encode);
    }

    public List<String> buscarHashDaContaComBloqueio(Long id) {
        return jdbc.queryForList(
                "SELECT senha_hash FROM acesso_conta WHERE funcionario_id=? FOR UPDATE",
                String.class,
                id);
    }

    public int alterarSenhaDaConta(String encode, Long id) {
        return jdbc.update(
                "UPDATE acesso_conta SET senha_hash=?, trocar_senha=FALSE, versao=versao+1 WHERE"
                        + " funcionario_id=?",
                encode,
                id);
    }
}
