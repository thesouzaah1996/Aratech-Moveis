package com.aratech.moveis.compras.cotacoes.repository;

import com.aratech.moveis.compras.cotacoes.dto.Cotacao;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.Map;

public interface CotacaoRepository {
    Pagina<Cotacao> listarCotacoes(int pagina, int tamanho, String busca);

    List<Opcao> listarPedidosAbertos();

    List<Map<String, Object>> bloquearPedidoAberto(Long pedidoId);

    Long inserirCotacao(
            Long pedidoId,
            String valor,
            BigDecimal valor2,
            String valor3,
            BigDecimal valor4,
            String valor5,
            BigDecimal valor6);

    Cotacao buscarCotacao(Long id);

    Cotacao bloquearCotacao(long id);

    int definirFornecedorEPreco(String valor, BigDecimal valor2, Long pedidoId);

    int registrarEscolha(int escolha, long id);

    int atualizarCotacaoCancelada(long id);
}
