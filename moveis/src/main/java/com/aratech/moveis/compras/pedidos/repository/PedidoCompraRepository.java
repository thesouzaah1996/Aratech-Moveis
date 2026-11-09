package com.aratech.moveis.compras.pedidos.repository;

import com.aratech.moveis.compras.pedidos.dto.Pedido;

import java.math.BigDecimal;
import java.time.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Map;

public interface PedidoCompraRepository {
    List<Pedido> listarTodos();

    List<Pedido> listarPorOrigem(String origem);

    List<Pedido> buscarPorId(boolean bloquear, Long id);

    List<Map<String, Object>> bloquearItemDeEstoque(boolean produto, Long id);

    int definirQuantidadeMinima(boolean produto, BigDecimal valor, Long id);

    String buscarFornecedorDoProduto(Long id);

    int inserirMonitoramento(String origem, Long id);

    Boolean bloquearMonitoramento(String origem, Long id);

    int atualizarMonitoramento(boolean baixo, String origem, Long id);

    int cancelarReposicaoPendente(String origem, Long id);

    int atualizarQuantidadeDaReposicao(
            BigDecimal quantidade, String origem, Long id, BigDecimal quantidade2);

    int inserirReposicaoSeNecessaria(
            String origem,
            Long id,
            String item,
            String codigo,
            BigDecimal quantidade,
            String unidade,
            String fornecedor,
            String origem2,
            Long id2);

    int inserirPedidoDaSolicitacao(
            Long id,
            String nomePeca,
            String codigoPeca,
            BigDecimal quantidade,
            String unidade,
            String solicitante);

    int registrarDecisao(String destino, Object valor, Long id, String nome, Long id2);

    Long inserirPedidoManual(
            String item,
            BigDecimal quantidade,
            String fornecedor,
            BigDecimal valorUnitario,
            LocalDate dataPedido,
            LocalDate dataEntrega,
            String nome);

    int atualizarPedidoManual(
            String item,
            BigDecimal quantidade,
            String fornecedor,
            BigDecimal valorUnitario,
            LocalDate dataPedido,
            LocalDate dataEntrega,
            Long id);

    int definirVencimento(LocalDate dataVencimento, Long id);

    int atualizarPedidoAguardandoAprovação(Long id);

    int cancelarRascunho(Long id);
}
