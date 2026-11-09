package com.aratech.moveis.comercial.pedidos.service.imp;

import com.aratech.moveis.comercial.pedidos.dto.NovaVenda;
import com.aratech.moveis.comercial.pedidos.dto.Venda;
import com.aratech.moveis.comercial.pedidos.repository.ComercialRepository;
import com.aratech.moveis.comercial.pedidos.service.ComercialService;
import com.aratech.moveis.financeiro.titulos.service.FinanceiroService;
import com.aratech.moveis.pcp.producao.service.ProducaoService;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ComercialServiceImp implements ComercialService {
    private final OperacaoService base;
    private final FinanceiroService financeiro;
    private final ComercialRepository repository;
    private final ProducaoService producao;

    public Pagina<Venda> vendas(int pagina, int tamanho, String busca) {
        return repository.listarVendas(pagina, tamanho, busca);
    }

    @Transactional
    public Venda venda(Long id, NovaVenda dto) {
        if (dto.entrega().isBefore(LocalDate.now(ZoneId.of("America/Sao_Paulo"))))
            throw new AuthException(400, "A previsão de entrega não pode estar no passado.");
        BigDecimal total = dto.valorUnitario().multiply(BigDecimal.valueOf(dto.quantidade()));
        if (total.compareTo(new BigDecimal("9999999999999.99")) > 0)
            throw new AuthException(400, "Valor total do pedido acima do limite financeiro.");
        return base.comando(
                dto.chaveOperacao(),
                "venda:" + id,
                dto,
                Venda.class,
                () -> {
                    Long registro = id;
                    if (id != null) {
                        var atual = repository.bloquearPedidoPorId(id);
                        base.versao(atual.versao(), dto.versao());
                        base.exigir(
                                atual.status().equals("RASCUNHO"),
                                "Somente rascunhos podem ser alterados.");
                    }
                    if (registro == null)
                        registro =
                                repository.inserirPedidoComercial(
                                        dto.cliente().trim(),
                                        dto.produto().trim(),
                                        dto.quantidade(),
                                        dto.valorUnitario(),
                                        dto.entrega(),
                                        dto.vencimento());
                    else
                        repository.atualizarPedidoComercial(
                                dto.cliente().trim(),
                                dto.produto().trim(),
                                dto.quantidade(),
                                dto.valorUnitario(),
                                dto.entrega(),
                                dto.vencimento(),
                                id);
                    base.auditar(
                            "COMERCIAL",
                            "SALVAR",
                            "VENDA",
                            registro,
                            "Pedido comercial em rascunho");
                    return repository.buscarPedidoPorId(registro);
                });
    }

    @Transactional
    public Venda vendaAcao(long id, String acao, Comando dto) {
        return base.comando(
                dto.chaveOperacao(),
                "venda:" + id + ":" + acao,
                dto,
                Venda.class,
                () -> {
                    var p = repository.bloquearPedidoPorId(id);
                    base.versao(p.versao(), dto.versao());
                    if (acao.equals("confirmar")) {
                        base.exigir(
                                p.status().equals("RASCUNHO"),
                                "Somente rascunhos podem ser confirmados.");
                        base.exigir(
                                !p.entrega()
                                        .isBefore(LocalDate.now(ZoneId.of("America/Sao_Paulo"))),
                                "Reveja a entrega antes de confirmar.");
                        producao.criarEncomenda(id, p.produto(), p.quantidade(), p.entrega());
                        financeiro.integrar(
                                "VENDA",
                                id,
                                "RECEBER",
                                "Pedido de venda #" + id,
                                p.cliente(),
                                p.valorUnitario().multiply(BigDecimal.valueOf(p.quantidade())),
                                p.vencimento());
                        repository.atualizarPedidoComercialConfirmado(id);
                    } else if (acao.equals("cancelar")) {
                        base.motivo(dto.motivo());
                        base.exigir(
                                Set.of("RASCUNHO", "CONFIRMADO").contains(p.status()),
                                "Pedido já em produção ou entregue não pode ser cancelado por este"
                                        + " fluxo.");
                        producao.cancelarEncomenda(id);
                        financeiro.cancelarOrigem("VENDA", id);
                        repository.atualizarPedidoComercialCancelado(id);

                    } else throw new AuthException(400, "Ação comercial inválida.");
                    base.auditar(
                            "COMERCIAL",
                            acao.toUpperCase(Locale.ROOT),
                            "VENDA",
                            id,
                            base.motivo(dto.motivo()));
                    return repository.buscarPedidoPorId(id);
                });
    }
}
