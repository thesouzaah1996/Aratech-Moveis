package com.aratech.moveis.compras.cotacoes.service.imp;

import com.aratech.moveis.compras.cotacoes.dto.Cotacao;
import com.aratech.moveis.compras.cotacoes.dto.NovaCotacao;
import com.aratech.moveis.compras.cotacoes.repository.CotacaoRepository;
import com.aratech.moveis.compras.cotacoes.service.CotacaoService;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CotacaoServiceImp implements CotacaoService {
    private final OperacaoService base;
    private final CotacaoRepository repository;

    public Pagina<Cotacao> listar(int pagina, int tamanho, String busca) {
        return repository.listarCotacoes(pagina, tamanho, busca);
    }

    public List<Opcao> pedidos() {
        return repository.listarPedidosAbertos();
    }

    private void opcional(String fornecedor, BigDecimal valor) {
        if ((fornecedor != null && !fornecedor.isBlank()) != (valor != null))
            throw new AuthException(400, "Informe fornecedor e preço juntos em cada proposta.");
    }

    @Transactional
    public Cotacao criar(NovaCotacao dto) {
        opcional(dto.fornecedor2(), dto.valor2());
        opcional(dto.fornecedor3(), dto.valor3());
        var nomes = new HashSet<String>();
        for (String nome : Arrays.asList(dto.fornecedor1(), dto.fornecedor2(), dto.fornecedor3()))
            if (nome != null && !nome.isBlank() && !nomes.add(nome.trim().toLowerCase(Locale.ROOT)))
                throw new AuthException(400, "Use fornecedores distintos nas propostas.");
        return base.comando(
                dto.chaveOperacao(),
                "cotacao:criar",
                dto,
                Cotacao.class,
                () -> {
                    var pedido = repository.bloquearPedidoAberto(dto.pedidoId());
                    base.exigir(
                            !pedido.isEmpty(),
                            "Selecione pedido em rascunho ou aguardando aprovação.");
                    Long id =
                            repository.inserirCotacao(
                                    dto.pedidoId(),
                                    dto.fornecedor1().trim(),
                                    dto.valor1(),
                                    dto.fornecedor2(),
                                    dto.valor2(),
                                    dto.fornecedor3(),
                                    dto.valor3());
                    base.auditar(
                            "COMPRAS",
                            "CADASTRAR",
                            "COTACAO",
                            id,
                            "Cotação vinculada ao pedido #" + dto.pedidoId());
                    return repository.buscarCotacao(id);
                });
    }

    @Transactional
    public Cotacao acao(long id, String acao, Comando dto) {
        return base.comando(
                dto.chaveOperacao(),
                "cotacao:" + id + ":" + acao,
                dto,
                Cotacao.class,
                () -> {
                    var p = repository.bloquearCotacao(id);
                    base.versao(p.versao(), dto.versao());
                    base.exigir(p.status().equals("ABERTA"), "Cotação já concluída ou cancelada.");
                    if (acao.equals("escolher")) {
                        if (dto.escolha() == null || dto.escolha() < 1 || dto.escolha() > 3)
                            throw new AuthException(400, "Selecione a proposta 1, 2 ou 3.");
                        int escolha = dto.escolha();
                        String fornecedor =
                                escolha == 1
                                        ? p.fornecedor1()
                                        : escolha == 2 ? p.fornecedor2() : p.fornecedor3();
                        BigDecimal valor =
                                escolha == 1 ? p.valor1() : escolha == 2 ? p.valor2() : p.valor3();
                        base.exigir(
                                fornecedor != null
                                        && !fornecedor.isBlank()
                                        && valor != null
                                        && valor.signum() > 0,
                                "A proposta escolhida precisa de fornecedor e preço válido.");
                        var pedido = repository.bloquearPedidoAberto(p.pedidoId());
                        base.exigir(
                                !pedido.isEmpty(),
                                "O pedido já foi decidido. Não é possível substituir o fornecedor"
                                        + " aprovado.");
                        repository.definirFornecedorEPreco(fornecedor.trim(), valor, p.pedidoId());
                        repository.registrarEscolha(escolha, id);
                    } else if (acao.equals("cancelar")) repository.atualizarCotacaoCancelada(id);
                    else throw new AuthException(400, "Ação de cotação inválida.");
                    base.auditar(
                            "COMPRAS",
                            acao.toUpperCase(Locale.ROOT),
                            "COTACAO",
                            id,
                            base.motivo(dto.motivo()));
                    return repository.buscarCotacao(id);
                });
    }
}
