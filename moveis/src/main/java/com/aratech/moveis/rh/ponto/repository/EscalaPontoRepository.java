package com.aratech.moveis.rh.ponto.repository;

import com.aratech.moveis.rh.ponto.dto.EscalaPontoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class EscalaPontoRepository {
    private final JdbcTemplate jdbc;
    public List<EscalaPontoDTO> listar(Long id) {
        return jdbc.query("SELECT dia_semana,horarios,extra_minutos FROM rh_ponto_escala WHERE funcionario_id=? ORDER BY dia_semana",
                (r,n) -> new EscalaPontoDTO(r.getInt(1), Arrays.asList(r.getString(2).split(",", -1)), r.getInt(3)), id);
    }
    public int jornada(Long id) {
        Integer horas = jdbc.queryForObject("SELECT jornada_semanal FROM funcionario WHERE id=?", Integer.class, id);
        if (horas == null) throw new com.aratech.moveis.security.exception.AuthException(400, "Cadastre a jornada semanal do funcionário antes da escala.");
        return horas * 60;
    }
    public void salvar(Long id, List<EscalaPontoDTO> dias) {
        for (var dia : dias) jdbc.update("INSERT INTO rh_ponto_escala(funcionario_id,dia_semana,horarios,extra_minutos) VALUES (?,?,?,?) ON CONFLICT(funcionario_id,dia_semana) DO UPDATE SET horarios=EXCLUDED.horarios,extra_minutos=EXCLUDED.extra_minutos",
                id, dia.diaSemana(), String.join(",", dia.horarios()), dia.extraMinutos());
    }
}
