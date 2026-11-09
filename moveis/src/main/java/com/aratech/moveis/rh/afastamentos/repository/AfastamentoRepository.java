package com.aratech.moveis.rh.afastamentos.repository;

import com.aratech.moveis.rh.afastamentos.dto.Afastamento;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Map;

public interface AfastamentoRepository {
    List<Opcao> listarFuncionariosAtivos();

    List<Map<String, Object>> bloquearFuncionarioAtivo(long id);

    Pagina<Afastamento> listarAfastamentos(int pagina, int tamanho, String busca);

    Long contarSobreposicoesAtivas(Long funcionarioId, LocalDate fim, LocalDate inicio);

    Long inserirAfastamento(
            Long funcionarioId, String tipo, LocalDate inicio, LocalDate fim, String valor);

    Afastamento buscarAfastamento(Long id);

    Afastamento bloquearAfastamento(long id);

    Long contarAprovadosSobrepostos(Long funcionarioId, long id, LocalDate fim, LocalDate inicio);

    int registrarDecisao(String destino, String valor, long id);
}
