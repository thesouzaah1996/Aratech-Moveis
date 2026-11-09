package com.aratech.moveis.rh.perfis.repository.imp;

import com.aratech.moveis.rh.perfis.dto.PerfilResumoDTO;
import com.aratech.moveis.rh.perfis.dto.UsuarioPerfilDTO;
import com.aratech.moveis.rh.perfis.repository.AtribuicaoPerfilRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AtribuicaoPerfilRepositoryImp implements AtribuicaoPerfilRepository {
    private final JdbcTemplate jdbc;
    private static final String FILTRO =
            " WHERE (strpos(lower(nome_completo), lower(?)) > 0 OR strpos(CAST(id AS TEXT), ?) ="
                    + " 1)";

    private List<PerfilResumoDTO> perfis(Long usuarioId) {
        return listarPerfisDoUsuario(usuarioId);
    }

    public Long contarFuncionariosPorBusca(String nome, String nome2) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM funcionario" + FILTRO, Long.class, nome, nome2);
    }

    public List<UsuarioPerfilDTO> listarFuncionariosPorBusca(
            String nome, String nome2, int tamanho, long valor) {
        return jdbc.query(
                "SELECT id, nome_completo, email, setor FROM funcionario"
                        + FILTRO
                        + " ORDER BY lower(nome_completo), id LIMIT ? OFFSET ?",
                (rs, n) ->
                        new UsuarioPerfilDTO(
                                rs.getLong("id"),
                                rs.getString("nome_completo"),
                                rs.getString("email"),
                                rs.getString("setor"),
                                perfis(rs.getLong("id"))),
                nome,
                nome2,
                tamanho,
                valor);
    }

    public List<UsuarioPerfilDTO> bloquearFuncionario(Long usuarioId) {
        return jdbc.query(
                "SELECT id, nome_completo, email, setor FROM funcionario WHERE id = ? FOR UPDATE",
                (rs, n) ->
                        new UsuarioPerfilDTO(
                                rs.getLong("id"),
                                rs.getString("nome_completo"),
                                rs.getString("email"),
                                rs.getString("setor"),
                                List.of()),
                usuarioId);
    }

    public List<PerfilResumoDTO> buscarPerfilPorNome(String replaceAll) {
        return jdbc.query(
                "SELECT id, nome FROM perfil_acesso WHERE lower(nome) = lower(?)",
                (rs, n) -> new PerfilResumoDTO(rs.getLong("id"), rs.getString("nome")),
                replaceAll);
    }

    public int inserirVinculo(Long usuarioId, Long id) {
        return jdbc.update(
                "INSERT INTO funcionario_perfil_acesso (funcionario_id, perfil_id) VALUES (?, ?) ON"
                        + " CONFLICT DO NOTHING",
                usuarioId,
                id);
    }

    public List<PerfilResumoDTO> listarPerfisDoUsuario(Long usuarioId) {
        return jdbc.query(
                "SELECT p.id, p.nome FROM perfil_acesso p JOIN funcionario_perfil_acesso f ON"
                        + " f.perfil_id = p.id WHERE f.funcionario_id = ? ORDER BY p.nome",
                (rs, n) -> new PerfilResumoDTO(rs.getLong("id"), rs.getString("nome")),
                usuarioId);
    }
}
