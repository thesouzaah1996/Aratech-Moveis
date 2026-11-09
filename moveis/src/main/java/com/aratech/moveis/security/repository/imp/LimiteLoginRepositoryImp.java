package com.aratech.moveis.security.repository.imp;

import com.aratech.moveis.security.model.TentativaLogin;
import com.aratech.moveis.security.repository.LimiteLoginRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LimiteLoginRepositoryImp implements LimiteLoginRepository {
    private final JdbcTemplate jdbc;

    public int removerTentativasAntigas() {
        return jdbc.update(
                "DELETE FROM acesso_tentativa WHERE janela < CURRENT_TIMESTAMP - INTERVAL '1 day'");
    }

    public int inserirTentativa(String chave) {
        return jdbc.update(
                "INSERT INTO acesso_tentativa(chave, janela, contagem) VALUES (?,"
                        + " CURRENT_TIMESTAMP, 0) ON CONFLICT DO NOTHING",
                chave);
    }

    public TentativaLogin bloquearTentativa(String chave) {
        return jdbc.queryForObject(
                "SELECT janela, contagem FROM acesso_tentativa WHERE chave=? FOR UPDATE",
                new org.springframework.jdbc.core.DataClassRowMapper<>(TentativaLogin.class),
                chave);
    }

    public int reiniciarJanela(String chave) {
        return jdbc.update(
                "UPDATE acesso_tentativa SET janela=CURRENT_TIMESTAMP, contagem=1 WHERE chave=?",
                chave);
    }

    public int incrementarTentativas(String chave) {
        return jdbc.update("UPDATE acesso_tentativa SET contagem=contagem+1 WHERE chave=?", chave);
    }

    public int removerTentativaDaConta(String valor) {
        return jdbc.update("DELETE FROM acesso_tentativa WHERE chave=?", valor);
    }
}
