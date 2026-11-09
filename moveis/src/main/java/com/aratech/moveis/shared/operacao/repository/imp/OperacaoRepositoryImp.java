package com.aratech.moveis.shared.operacao.repository.imp;

import com.aratech.moveis.shared.operacao.model.ComandoPersistido;
import com.aratech.moveis.shared.operacao.repository.OperacaoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OperacaoRepositoryImp implements OperacaoRepository {
    private final JdbcTemplate jdbc;

    public int inserirComando(UUID chave, String escopo, String assinatura) {
        return jdbc.update(
                "INSERT INTO gestao_comando(chave,escopo,assinatura) VALUES (?,?,?) ON CONFLICT DO"
                        + " NOTHING",
                chave,
                escopo,
                assinatura);
    }

    public ComandoPersistido bloquearComando(UUID chave) {
        return jdbc.queryForObject(
                "SELECT escopo,assinatura,resposta FROM gestao_comando WHERE chave=? FOR UPDATE",
                new org.springframework.jdbc.core.DataClassRowMapper<>(ComandoPersistido.class),
                chave);
    }

    public int atualizarComando(String writeValueAsString, UUID chave) {
        return jdbc.update(
                "UPDATE gestao_comando SET resposta=? WHERE chave=?", writeValueAsString, chave);
    }

    public int inserirAuditoria(
            String modulo,
            String acao,
            String entidade,
            Long id,
            Long id2,
            String nome,
            String detalhe) {
        return jdbc.update(
                "INSERT INTO"
                    + " gestao_auditoria(modulo,acao,entidade,registro_id,usuario_id,usuario,detalhe)"
                    + " VALUES (?,?,?,?,?,?,?)",
                modulo,
                acao,
                entidade,
                id,
                id2,
                nome,
                detalhe);
    }

    public Integer consultarParametro(String chave) {
        return jdbc.queryForObject(
                "SELECT valor FROM gestao_parametro WHERE chave=?", Integer.class, chave);
    }
}
