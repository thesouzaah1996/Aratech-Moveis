package com.aratech.moveis.security.repository.imp;

import com.aratech.moveis.security.repository.GestaoAcessoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GestaoAcessoRepositoryImp implements GestaoAcessoRepository {
    private final JdbcTemplate jdbc;

    public Boolean administrador(Long id) {
        return jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM acesso_conta WHERE funcionario_id=? AND administrador"
                        + " IS TRUE)",
                Boolean.class,
                id);
    }

    public List<String> listarPermissoesDoUsuario(Long id) {
        return jdbc.queryForList(
                "SELECT DISTINCT p.permissao FROM perfil_permissao_acesso p JOIN"
                        + " funcionario_perfil_acesso f ON f.perfil_id=p.perfil_id WHERE"
                        + " f.funcionario_id=?",
                String.class,
                id);
    }

    public List<String> buscarEmailPessoal(Long id) {
        return jdbc.queryForList("SELECT email FROM funcionario WHERE id=?", String.class, id);
    }

    public Boolean usaEmailPessoalComoLogin(Long id) {
        return jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM acesso_conta WHERE funcionario_id=? AND"
                        + " email_corporativo IS NULL)",
                Boolean.class,
                id);
    }

    public Long contarAdministradoresAtivos() {
        return jdbc.queryForObject(
                "SELECT count(*) FROM acesso_conta c JOIN funcionario f ON f.id=c.funcionario_id"
                        + " WHERE c.administrador IS TRUE AND f.ativo IS TRUE",
                Long.class);
    }

    public Boolean existePerfil(Long perfilId) {
        return jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM perfil_acesso WHERE id=?)", Boolean.class, perfilId);
    }

    public List<String> listarPermissoesDoPerfil(Long perfilId) {
        return jdbc.queryForList(
                "SELECT permissao FROM perfil_permissao_acesso WHERE perfil_id=?",
                String.class,
                perfilId);
    }

    public List<Long> bloquearPerfilPorNome(String replaceAll) {
        return jdbc.queryForList(
                "SELECT id FROM perfil_acesso WHERE lower(nome)=lower(?) FOR UPDATE",
                Long.class,
                replaceAll);
    }

    public List<Long> bloquearPerfilPorId(Long perfilId) {
        return jdbc.queryForList(
                "SELECT id FROM perfil_acesso WHERE id=? FOR UPDATE", Long.class, perfilId);
    }

    public int removerPermissoesDoPerfil(Long perfilId) {
        return jdbc.update("DELETE FROM perfil_permissao_acesso WHERE perfil_id=?", perfilId);
    }

    public int inserirPermissao(Long perfilId, String permissao) {
        return jdbc.update(
                "INSERT INTO perfil_permissao_acesso(perfil_id, permissao) VALUES (?, ?)",
                perfilId,
                permissao);
    }

    public int removerVinculo(Long usuarioId, Long perfilId) {
        return jdbc.update(
                "DELETE FROM funcionario_perfil_acesso WHERE funcionario_id=? AND perfil_id=?",
                usuarioId,
                perfilId);
    }
}
