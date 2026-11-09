package com.aratech.moveis.rh.ponto.repository;

import com.aratech.moveis.rh.ponto.dto.DiaPontoDTO;
import com.aratech.moveis.rh.ponto.dto.FuncionarioPontoDTO;
import com.aratech.moveis.rh.ponto.dto.HistoricoPontoDTO;

import java.time.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Map;

public interface PontoRepository {
    List<FuncionarioPontoDTO> consultarFuncionario(Long id);

    List<DiaPontoDTO> consultarDia(Long id, LocalDate atDay, LocalDate atEndOfMonth);

    List<HistoricoPontoDTO> consultarHistorico(Long id, LocalDate data);

    List<Long> bloquearFuncionario(Long id);

    List<Map.Entry<String, Long>> bloquearDia(Long id, LocalDate data);

    int inserirDia(Long id, LocalDate data, String novos);

    int inserirHistorico(
            Long id,
            LocalDate data,
            String antes,
            String novos,
            String motivo,
            Long id2,
            String nome);
}
