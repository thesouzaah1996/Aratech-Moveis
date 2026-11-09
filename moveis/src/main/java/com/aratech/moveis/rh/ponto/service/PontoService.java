package com.aratech.moveis.rh.ponto.service;

import com.aratech.moveis.rh.ponto.dto.EscalaPontoDTO;
import com.aratech.moveis.rh.ponto.dto.MarcacaoPontoDTO;

import com.aratech.moveis.rh.ponto.dto.DiaPontoDTO;
import com.aratech.moveis.rh.ponto.dto.EdicaoPontoDTO;
import com.aratech.moveis.rh.ponto.dto.FuncionarioPontoDTO;
import com.aratech.moveis.rh.ponto.dto.HistoricoPontoDTO;
import com.aratech.moveis.security.dto.UsuarioSessao;

import java.time.*;
import java.util.*;

public interface PontoService {

    List<EscalaPontoDTO> escala(Long id);
    void salvarEscala(Long id, List<EscalaPontoDTO> dias, UsuarioSessao autor);

    DiaPontoDTO marcar(MarcacaoPontoDTO dto, UsuarioSessao usuario);

    FuncionarioPontoDTO funcionario(Long id);

    List<DiaPontoDTO> listar(Long id, String mes);

    List<HistoricoPontoDTO> historico(Long id, LocalDate data);

    DiaPontoDTO salvar(Long id, LocalDate data, EdicaoPontoDTO dto, UsuarioSessao autor);
}
