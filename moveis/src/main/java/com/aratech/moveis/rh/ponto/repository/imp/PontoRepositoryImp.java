package com.aratech.moveis.rh.ponto.repository.imp;

import com.aratech.moveis.rh.ponto.dto.DiaPontoDTO;
import com.aratech.moveis.rh.ponto.dto.FuncionarioPontoDTO;
import com.aratech.moveis.rh.ponto.dto.HistoricoPontoDTO;
import com.aratech.moveis.rh.ponto.repository.PontoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PontoRepositoryImp implements PontoRepository {
    private final JdbcTemplate jdbc;

    public List<FuncionarioPontoDTO> consultarFuncionario(Long id) {
        return jdbc.query(
                "SELECT id,nome_completo,cargo,setor FROM funcionario WHERE id=?",
                (r, n) ->
                        new FuncionarioPontoDTO(
                                r.getLong(1), r.getString(2), r.getString(3), r.getString(4)),
                id);
    }

    public List<DiaPontoDTO> consultarDia(Long id, LocalDate atDay, LocalDate atEndOfMonth) {
        return jdbc.query(
                "SELECT data,horarios,versao FROM rh_ponto_dia WHERE funcionario_id=? AND data>=?"
                        + " AND data<=? ORDER BY data",
                (r, n) ->
                        new DiaPontoDTO(
                                r.getDate(1).toLocalDate(),
                                Arrays.asList(r.getString(2).split(",", -1)),
                                r.getLong(3),
                                true),
                id,
                atDay,
                atEndOfMonth);
    }

    public List<HistoricoPontoDTO> consultarHistorico(Long id, LocalDate data) {
        return jdbc.query(
                "SELECT id,data,anteriores,novos,motivo,autor_nome,alterado_em FROM"
                        + " rh_ponto_historico WHERE funcionario_id=? AND data=? ORDER BY id DESC",
                (r, n) ->
                        new HistoricoPontoDTO(
                                r.getLong(1),
                                r.getDate(2).toLocalDate(),
                                r.getString(3),
                                r.getString(4),
                                r.getString(5),
                                r.getString(6),
                                r.getTimestamp(7).toInstant().toString()),
                id,
                data);
    }

    public List<Long> bloquearFuncionario(Long id) {
        return jdbc.queryForList(
                "SELECT id FROM funcionario WHERE id=? FOR UPDATE", Long.class, id);
    }

    public List<Map.Entry<String, Long>> bloquearDia(Long id, LocalDate data) {
        return jdbc.query(
                "SELECT horarios,versao FROM rh_ponto_dia WHERE funcionario_id=? AND data=? FOR"
                        + " UPDATE",
                (r, n) -> Map.entry(r.getString(1), r.getLong(2)),
                id,
                data);
    }

    public int inserirDia(Long id, LocalDate data, String novos) {
        return jdbc.update(
                "INSERT INTO rh_ponto_dia(funcionario_id,data,horarios,versao) VALUES (?,?,?,1) ON"
                        + " CONFLICT(funcionario_id,data) DO UPDATE SET"
                        + " horarios=EXCLUDED.horarios,versao=rh_ponto_dia.versao+1",
                id,
                data,
                novos);
    }

    public int inserirHistorico(
            Long id,
            LocalDate data,
            String antes,
            String novos,
            String motivo,
            Long id2,
            String nome) {
        return jdbc.update(
                "INSERT INTO"
                    + " rh_ponto_historico(funcionario_id,data,anteriores,novos,motivo,autor_id,autor_nome)"
                    + " VALUES (?,?,?,?,?,?,?)",
                id,
                data,
                antes,
                novos,
                motivo,
                id2,
                nome);
    }
}
