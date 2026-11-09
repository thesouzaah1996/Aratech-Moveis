package com.aratech.moveis.compras.pedidos.service.imp;

import com.aratech.moveis.almoxarifado.produtos.model.Produto;
import com.aratech.moveis.compras.pedidos.dto.Cadastro;
import com.aratech.moveis.compras.pedidos.dto.Decisao;
import com.aratech.moveis.compras.pedidos.dto.Minimo;
import com.aratech.moveis.compras.pedidos.dto.Pedido;
import com.aratech.moveis.compras.pedidos.repository.PedidoCompraRepository;
import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;
import com.aratech.moveis.financeiro.titulos.service.FinanceiroService;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.estoque.model.PecaEstoque;
import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PedidoCompraServiceImp implements PedidoCompraService {
    private final PedidoCompraRepository repository;
    private final FinanceiroService financeiro;

    public List<Pedido> listar(String origem) {
        if (origem != null && !Set.of("ALMOXARIFADO", "MANUTENCAO").contains(origem))
            throw new AuthException(400, "Origem inválida.");
        return origem == null ? repository.listarTodos() : repository.listarPorOrigem(origem);
    }

    private Pedido buscar(Long id, boolean bloquear) {
        return repository.buscarPorId(bloquear, id).stream()
                .findFirst()
                .orElseThrow(() -> new AuthException(404, "Pedido não encontrado."));
    }

    @Transactional
    public Minimo minimo(String origem, Long id, BigDecimal valor) {
        boolean produto = origem.equals("ALMOXARIFADO");
        BigDecimal limite = new BigDecimal(produto ? "999999999" : "999999999998.999");
        if (valor != null
                && (valor.signum() < 0
                        || valor.compareTo(limite) > 0
                        || valor.stripTrailingZeros().scale() > (produto ? 0 : 3)))
            throw new AuthException(
                    400,
                    produto
                            ? "Informe um mínimo inteiro de 0 a 999999999, ou deixe vazio para"
                                    + " desativar."
                            : "Informe um mínimo não negativo, até 999999999998.999 e com até três"
                                    + " casas decimais, ou deixe vazio para desativar.");

        var linhas = repository.bloquearItemDeEstoque(produto, id);
        if (linhas.isEmpty()) throw new AuthException(404, "Item ativo não encontrado.");
        repository.definirQuantidadeMinima(produto, valor, id);
        var r = linhas.getFirst();
        String fornecedor = produto ? repository.buscarFornecedorDoProduto(id) : null;
        monitorar(
                origem,
                id,
                (String) r.get("nome"),
                (String) r.get(produto ? "sku" : "codigo"),
                new BigDecimal(r.get("quantidade").toString()),
                valor,
                produto ? "un" : (String) r.get("unidade"),
                fornecedor);
        return new Minimo(valor);
    }

    public void produto(Produto p) {
        monitorar(
                "ALMOXARIFADO",
                p.getId(),
                p.getNome(),
                p.getSku(),
                BigDecimal.valueOf(p.getQuantidade()),
                !Boolean.TRUE.equals(p.getAtivo()) || p.getQuantidadeMinima() == null
                        ? null
                        : BigDecimal.valueOf(p.getQuantidadeMinima()),
                "un",
                p.getFornecedor().getNome());
    }

    public void peca(PecaEstoque p) {
        monitorar(
                "MANUTENCAO",
                p.getId(),
                p.getNome(),
                p.getCodigo(),
                p.getQuantidade(),
                p.isAtivo() ? p.getQuantidadeMinima() : null,
                p.getUnidade(),
                null);
    }

    private void monitorar(
            String origem,
            Long id,
            String item,
            String codigo,
            BigDecimal saldo,
            BigDecimal minimo,
            String unidade,
            String fornecedor) {
        repository.inserirMonitoramento(origem, id);
        boolean anterior = Boolean.TRUE.equals(repository.bloquearMonitoramento(origem, id));
        boolean baixo = minimo != null && saldo.compareTo(minimo) <= 0;
        repository.atualizarMonitoramento(baixo, origem, id);
        if (!baixo) {
            repository.cancelarReposicaoPendente(origem, id);
            return;
        }
        BigDecimal quantidade = minimo.subtract(saldo).add(BigDecimal.ONE);
        if (anterior) {
            repository.atualizarQuantidadeDaReposicao(quantidade, origem, id, quantidade);
            return;
        }
        repository.inserirReposicaoSeNecessaria(
                origem, id, item, codigo, quantidade, unidade, fornecedor, origem, id);
    }

    public void solicitacao(Chamado c) {
        repository.inserirPedidoDaSolicitacao(
                c.getId(),
                c.getNomePeca(),
                c.getCodigoPeca(),
                c.getQuantidade(),
                c.getUnidade(),
                c.getSolicitante());
    }

    @Transactional
    public Pedido decidir(Long id, String acao, Decisao dto, UsuarioSessao autor) {
        if (!Permissoes.permite(autor, "COMPRAS_PEDIDOS.FINALIZAR"))
            throw new AuthException(403, "Você não possui permissão para decidir pedidos.");
        var p = buscar(id, true);
        versao(p, dto.versao());
        String destino =
                switch (acao) {
                    case "aprovar" -> "Aprovado";
                    case "reprovar" -> "Reprovado";
                    case "receber" -> "Recebido";
                    default -> throw new AuthException(400, "Ação inválida.");
                };
        if (!p.status().equals(acao.equals("receber") ? "Aprovado" : "Aguardando Aprovação"))
            throw new AuthException(409, "Este pedido não permite esta ação no status atual.");
        if (acao.equals("aprovar") || acao.equals("receber")) {
            if (p.fornecedor() == null
                    || p.fornecedor().isBlank()
                    || p.valorUnitario() == null
                    || p.valorUnitario().signum() <= 0
                    || p.dataEntrega() == null
                    || p.dataVencimento() == null)
                throw new AuthException(
                        409,
                        "Complete fornecedor, preço, entrega e vencimento negociado antes de"
                                + " aprovar ou receber. Esses dados geram a conta a pagar.");
            if (p.quantidade()
                            .multiply(p.valorUnitario())
                            .compareTo(new BigDecimal("9999999999999.99"))
                    > 0) throw new AuthException(409, "Valor total acima do limite financeiro.");
        }
        String motivo = dto.motivo() == null ? null : dto.motivo().strip();
        if (acao.equals("reprovar")
                && (motivo == null || motivo.length() < 5 || motivo.length() > 1000))
            throw new AuthException(
                    400, "Informe por que o pedido foi reprovado (5 a 1000 caracteres).");
        repository.registrarDecisao(
                destino, acao.equals("reprovar") ? motivo : null, autor.id(), autor.nome(), id);
        if (acao.equals("receber"))
            financeiro.integrar(
                    "COMPRA",
                    id,
                    "PAGAR",
                    "Pedido de compra PC-" + id,
                    p.fornecedor(),
                    p.quantidade().multiply(p.valorUnitario()),
                    p.dataVencimento(),
                    autor);
        return buscar(id, false);
    }

    @Transactional
    public Pedido salvar(Long id, Cadastro dto, UsuarioSessao autor) {
        if (dto == null
                || dto.item() == null
                || dto.item().strip().length() < 2
                || dto.item().length() > 150
                || dto.quantidade() == null
                || dto.quantidade().signum() <= 0
                || dto.quantidade().compareTo(new BigDecimal("999999999999.999")) > 0
                || dto.quantidade().stripTrailingZeros().scale() > 3)
            throw new AuthException(
                    400, "Confira o item e a quantidade positiva com até três casas decimais.");
        if (dto.fornecedor() == null
                || dto.fornecedor().strip().isEmpty()
                || dto.fornecedor().length() > 150
                || dto.valorUnitario() == null
                || dto.valorUnitario().signum() <= 0
                || dto.valorUnitario().compareTo(new BigDecimal("9999999999999.99")) > 0
                || dto.valorUnitario().stripTrailingZeros().scale() > 2
                || dto.dataPedido() == null
                || dto.dataEntrega() == null
                || dto.dataEntrega().isBefore(dto.dataPedido()))
            throw new AuthException(
                    400,
                    "Confira fornecedor, valor unitário e entrega igual ou posterior à data do"
                            + " pedido.");
        if (dto.dataVencimento() == null || dto.dataVencimento().isBefore(dto.dataPedido()))
            throw new AuthException(
                    400, "Informe o vencimento negociado, igual ou posterior à data do pedido.");
        if (id == null)
            id =
                    repository.inserirPedidoManual(
                            dto.item().strip(),
                            dto.quantidade(),
                            dto.fornecedor().strip(),
                            dto.valorUnitario(),
                            dto.dataPedido(),
                            dto.dataEntrega(),
                            autor.nome());
        else {
            var p = buscar(id, true);
            versao(p, dto.versao());
            if (!Set.of("Rascunho", "Aguardando Aprovação").contains(p.status()))
                throw new AuthException(
                        409,
                        "Somente rascunhos e pedidos aguardando aprovação podem ser editados.");
            if (!p.tipo().equals("MANUAL") && (!p.item().equals(dto.item().strip())))
                throw new AuthException(
                        409, "O item do pedido de origem não pode ser substituído.");
            repository.atualizarPedidoManual(
                    dto.item().strip(),
                    dto.quantidade(),
                    dto.fornecedor().strip(),
                    dto.valorUnitario(),
                    dto.dataPedido(),
                    dto.dataEntrega(),
                    id);
        }
        repository.definirVencimento(dto.dataVencimento(), id);
        return buscar(id, false);
    }

    @Transactional
    public Pedido enviar(Long id, long versao) {
        var p = buscar(id, true);
        versao(p, versao);
        if (!p.status().equals("Rascunho"))
            throw new AuthException(409, "Somente rascunhos podem ser enviados.");
        repository.atualizarPedidoAguardandoAprovação(id);
        return buscar(id, false);
    }

    @Transactional
    public Pedido cancelar(Long id, long versao) {
        var p = buscar(id, true);
        versao(p, versao);
        if (!p.tipo().equals("MANUAL") || !p.status().equals("Rascunho"))
            throw new AuthException(409, "Somente rascunhos manuais podem ser removidos.");
        repository.cancelarRascunho(id);
        return buscar(id, false);
    }

    private void versao(Pedido p, long versao) {
        if (p.versao() != versao)
            throw new AuthException(
                    409,
                    "O pedido foi atualizado por outra operação. Recarregue a lista antes de"
                            + " continuar.");
    }
}
