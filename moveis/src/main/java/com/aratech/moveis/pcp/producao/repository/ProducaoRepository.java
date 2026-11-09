package com.aratech.moveis.pcp.producao.repository;

import com.aratech.moveis.pcp.producao.dto.Carga;
import com.aratech.moveis.pcp.producao.dto.EstadoOrdem;
import com.aratech.moveis.pcp.producao.dto.Linha;
import com.aratech.moveis.pcp.producao.dto.Ordem;
import com.aratech.moveis.pcp.producao.model.ReservaCapacidade;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.math.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Map;

public interface ProducaoRepository {
    Pagina<Linha> listarLinhas(int pagina, int tamanho, String busca);

    Pagina<Ordem> listarOrdens(String filtro, int pagina, int tamanho, String busca);

    List<Opcao> listarLinhasAtivas();

    Ordem buscarOrdem(long id);

    Long inserirLinha(String nome, BigDecimal horasDia);

    Linha bloquearLinhaPorId(Long registro);

    Long contarOrdensAbertasDaLinha(Long registro);

    int atualizarLinha(String nome, BigDecimal horasDia, Long registro);

    Linha buscarLinha(Long registro);

    List<ReservaCapacidade> listarReservasDeCapacidade(
            Long linhaId, Long ignorar, Long ignorar2, LocalDate previsao, LocalDate inicio);

    EstadoOrdem bloquearOrdemPorId(Long id);

    Long inserirOrdem(
            String produto,
            Integer quantidade,
            LocalDate inicio,
            LocalDate previsao,
            String responsavel,
            Long linhaId,
            BigDecimal horasPrevistas);

    int atualizarOrdem(
            String produto,
            Integer quantidade,
            LocalDate inicio,
            LocalDate previsao,
            String responsavel,
            Long linhaId,
            BigDecimal horasPrevistas,
            Long registro);

    Long contarRequisicoesPendentes(long id);

    Long contarRequisicoesAtendidas(long id);

    int atualizarRequisicaoRecusada(long id);

    int atualizarOrdemEmProducao(long id);

    int atualizarPedidoComercialEmProducao(Long pedidoId);

    int atualizarOrdemFinalizada(long id);

    int atualizarPedidoComercialPronto(Long pedidoId);

    int atualizarOrdemCancelada(long id);

    List<Carga> consultarCargaDasLinhas();

    Map<String, Object> consultarIndicadoresDeProducao();

    int inserirOrdemDaEncomenda(String produto, int quantidade, LocalDate entrega, long id);

    List<String> bloquearOrdensDaEncomenda(long id);

    Long contarMateriaisEntreguesDaEncomenda(long id);

    int cancelarOrdensDaEncomenda(long id);

    int recusarPendentesDaEncomenda(long id);
}
