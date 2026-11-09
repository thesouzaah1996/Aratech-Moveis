package com.aratech.moveis.security.repository.imp;

import com.aratech.moveis.security.dto.Credencial;
import com.aratech.moveis.security.model.DadosConta;
import com.aratech.moveis.security.repository.ContasRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ContasRepositoryImp implements ContasRepository {
    private final JdbcTemplate jdbc;

    @Override
    public List<Credencial> consultarAdministrador(String email) {
        return jdbc.query(
                "SELECT id,senha_hash hash FROM acesso_administrador WHERE login=lower(?) AND ativo"
                        + " IS TRUE",
                new DataClassRowMapper<>(Credencial.class),
                email);
    }

    @Override
    public List<Credencial> consultarConta(String email) {
        return jdbc.query(
                "SELECT c.funcionario_id id,c.senha_hash hash FROM acesso_conta c JOIN funcionario"
                    + " f ON f.id=c.funcionario_id WHERE"
                    + " lower(COALESCE(c.email_corporativo,f.email))=lower(?) AND f.ativo IS TRUE",
                new DataClassRowMapper<>(Credencial.class),
                email);
    }

    @Override
    public List<DadosConta> consultarAdministradorAtivo() {
        return jdbc.query(
                "SELECT id,'Administrador do Sistema' nome,login email,TRUE"
                    + " administrador,versao,trocar_senha FROM acesso_administrador WHERE id=0 AND"
                    + " ativo IS TRUE",
                new DataClassRowMapper<>(DadosConta.class));
    }

    @Override
    public List<DadosConta> consultarFuncionarioAtivo(Long id) {
        return jdbc.query(
                "SELECT f.id,f.nome_completo nome,COALESCE(c.email_corporativo,f.email)"
                    + " email,c.administrador,c.versao,c.trocar_senha FROM acesso_conta c JOIN"
                    + " funcionario f ON f.id=c.funcionario_id WHERE f.id=? AND f.ativo IS TRUE",
                new DataClassRowMapper<>(DadosConta.class),
                id);
    }

    @Override
    public List<String> consultarPerfil(Long id) {
        return jdbc.queryForList(
                "SELECT p.nome FROM perfil_acesso p JOIN funcionario_perfil_acesso f ON"
                        + " f.perfil_id=p.id WHERE f.funcionario_id=?",
                String.class,
                id);
    }

    @Override
    public List<String> consultarPermissao(Long id) {
        return jdbc.queryForList(
                "SELECT DISTINCT p.permissao FROM perfil_permissao_acesso p JOIN"
                        + " funcionario_perfil_acesso f ON f.perfil_id=p.perfil_id WHERE"
                        + " f.funcionario_id=?",
                String.class,
                id);
    }
}
