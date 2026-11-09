package com.aratech.moveis.shared.apoio.repository.imp;

import com.aratech.moveis.shared.apoio.dto.FuncionarioPonto;
import com.aratech.moveis.shared.apoio.repository.ApoioRepository;
import com.aratech.moveis.shared.dto.Opcao;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ApoioRepositoryImp implements ApoioRepository {
    private final JdbcTemplate jdbc;

    private List<Opcao> opcoes(String sql) {
        return jdbc.query(sql, (r, n) -> new Opcao(r.getLong(1), r.getString(2)));
    }

    public List<Opcao> categorias() {
        return opcoes("SELECT id,nome FROM categoria WHERE ativo IS TRUE ORDER BY nome");
    }

    public List<Opcao> fornecedores() {
        return opcoes("SELECT id,nome FROM fornecedor WHERE ativo IS TRUE ORDER BY nome");
    }

    public List<Opcao> mecanicos() {
        return opcoes("SELECT id,nome FROM manutencao_mecanico WHERE ativo IS TRUE ORDER BY nome");
    }

    public List<FuncionarioPonto> ponto() {
        return jdbc.query(
                "SELECT id,nome_completo nome,cargo,setor FROM funcionario WHERE ativo IS TRUE"
                        + " ORDER BY nome_completo",
                new org.springframework.jdbc.core.DataClassRowMapper<>(FuncionarioPonto.class));
    }
}
