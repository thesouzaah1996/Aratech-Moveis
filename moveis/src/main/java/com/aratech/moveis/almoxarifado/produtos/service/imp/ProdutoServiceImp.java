package com.aratech.moveis.almoxarifado.produtos.service.imp;

import com.aratech.moveis.almoxarifado.categorias.repository.CategoriaRepository;
import com.aratech.moveis.almoxarifado.fornecedores.repository.FornecedorRepository;
import com.aratech.moveis.almoxarifado.produtos.dto.MovimentacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.MovimentacaoResumoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.ProdutoAtualizacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.ProdutoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.ProdutoResumoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.Response;
import com.aratech.moveis.almoxarifado.produtos.enums.TipoMovimentacao;
import com.aratech.moveis.almoxarifado.produtos.exception.ProdutoException;
import com.aratech.moveis.almoxarifado.produtos.model.MovimentacaoEstoque;
import com.aratech.moveis.almoxarifado.produtos.model.Produto;
import com.aratech.moveis.almoxarifado.produtos.repository.MovimentacaoEstoqueRepository;
import com.aratech.moveis.almoxarifado.produtos.repository.ProdutoRepository;
import com.aratech.moveis.almoxarifado.produtos.service.ProdutoService;
import com.aratech.moveis.almoxarifado.produtos.validation.ProdutoValidator;
import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoServiceImp implements ProdutoService {
    private final ProdutoRepository repository;
    private final MovimentacaoEstoqueRepository movimentos;
    private final CategoriaRepository categorias;
    private final FornecedorRepository fornecedores;
    private final ProdutoValidator validator;
    private final PedidoCompraService compras;

    @Override
    @Transactional
    public Response addProduto(ProdutoDTO dto) {
        validator.validar(dto);
        if (repository.existsBySkuIgnoreCase(dto.getSku())) {
            throw conflito("SKU ja cadastrado, inclusive em produtos arquivados.");
        }
        validarVencimentoNovo(dto.getVencimentoProduto());
        var produto =
                Produto.builder()
                        .nome(dto.getNome())
                        .sku(dto.getSku())
                        .quantidade(dto.getQuantidade().intValueExact())
                        .localArmazenamento(dto.getLocalArmazenamento())
                        .descricao(dto.getDescricao())
                        .vencimentoProduto(dto.getVencimentoProduto())
                        .criadoEm(LocalDateTime.now())
                        .build();
        vincular(produto, dto.getCategoriaID(), dto.getFornecedorID());
        var salvo = repository.saveAndFlush(produto);
        compras.produto(salvo);
        registrar(
                salvo,
                TipoMovimentacao.INICIAL,
                salvo.getQuantidade(),
                0,
                UUID.randomUUID(),
                "Saldo inicial do cadastro");
        return resposta(salvo, HttpStatus.CREATED, "Produto cadastrado com sucesso.");
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllProdutos() {
        return getAllProdutos(0, 20, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllProdutos(int pagina, int tamanho, String busca) {
        validator.validarPaginacao(pagina, tamanho);
        var resultado =
                repository.buscar(
                        validator.validarBusca(busca),
                        PageRequest.of(pagina, tamanho, Sort.by("id").ascending()));
        return Response.builder()
                .status(200)
                .produtos(resultado.getContent().stream().map(this::resumo).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(resultado.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response getProdutoById(Long id) {
        validator.validarId(id);
        return resposta(buscar(id, false), HttpStatus.OK, "Produto encontrado.");
    }

    @Override
    @Transactional
    public Response updateProduto(ProdutoAtualizacaoDTO dto, Long id) {
        validator.validarId(id);
        validator.validar(dto);
        var produto = buscar(id, true);
        validarAtivo(produto);
        if (!produto.getSku().equals(dto.getSku())) throw conflito("O SKU nao pode ser alterado.");
        if (!Objects.equals(produto.getVencimentoProduto(), dto.getVencimentoProduto())) {
            validarVencimentoNovo(dto.getVencimentoProduto());
        }
        vincular(produto, dto.getCategoriaID(), dto.getFornecedorID());
        produto.setNome(dto.getNome());
        produto.setLocalArmazenamento(dto.getLocalArmazenamento());
        produto.setDescricao(dto.getDescricao());
        produto.setVencimentoProduto(dto.getVencimentoProduto());
        var atualizado = repository.saveAndFlush(produto);
        compras.produto(atualizado);
        return resposta(atualizado, HttpStatus.OK, "Produto atualizado com sucesso.");
    }

    @Override
    @Transactional
    public Response deleteProduto(Long id) {
        validator.validarId(id);
        var produto = buscar(id, true);
        validarSaldo(produto);
        if (produto.getQuantidade() != 0)
            throw conflito("Retire todo o estoque antes de arquivar o produto.");
        if (Boolean.FALSE.equals(produto.getAtivo()))
            return resposta(produto, HttpStatus.OK, "Produto ja esta arquivado.");
        validarAtivo(produto);
        produto.setAtivo(false);
        var atualizado = repository.saveAndFlush(produto);
        compras.produto(atualizado);
        return resposta(atualizado, HttpStatus.OK, "Produto arquivado. Historico preservado.");
    }

    @Override
    @Transactional
    public Response entrada(Long id, MovimentacaoDTO dto) {
        return movimentar(id, dto, TipoMovimentacao.ENTRADA);
    }

    @Override
    @Transactional
    public Response saida(Long id, MovimentacaoDTO dto) {
        return movimentar(id, dto, TipoMovimentacao.SAIDA);
    }

    private Response movimentar(Long id, MovimentacaoDTO dto, TipoMovimentacao tipo) {
        validator.validarId(id);
        validator.validar(dto);
        var produto = buscar(id, true);
        int quantidade = dto.getQuantidade().intValueExact();
        var anterior = movimentos.findByChaveOperacao(dto.getChaveOperacao());
        if (anterior.isPresent()) {
            var registrado = anterior.get();
            if (!registrado.getProduto().getId().equals(id)
                    || registrado.getTipo() != tipo
                    || registrado.getQuantidade() != quantidade
                    || !Objects.equals(registrado.getMotivo(), dto.getMotivo()))
                throw conflito("Chave de operacao ja utilizada com outros dados.");
            return resposta(
                    produto,
                    HttpStatus.OK,
                    "Operacao ja registrada; saldo nao alterado novamente.");
        }
        validarAtivo(produto);
        validarSaldo(produto);
        if (tipo == TipoMovimentacao.ENTRADA) {
            validarVencimentoNovo(produto.getVencimentoProduto());
            vincular(produto, produto.getCategoria().getId(), produto.getFornecedor().getId());
        }
        int saldoAnterior = produto.getQuantidade();
        long saldo =
                tipo == TipoMovimentacao.ENTRADA
                        ? (long) saldoAnterior + quantidade
                        : (long) saldoAnterior - quantidade;
        if (saldo < 0) throw conflito("Quantidade maior que o estoque disponivel.");
        if (saldo > 1000000000)
            throw conflito("O estoque ultrapassa o limite de 1 bilhao de unidades.");
        produto.setQuantidade((int) saldo);
        var salvo = repository.saveAndFlush(produto);
        compras.produto(salvo);
        registrar(salvo, tipo, quantidade, saldoAnterior, dto.getChaveOperacao(), dto.getMotivo());
        return resposta(salvo, HttpStatus.OK, "Movimentacao registrada com sucesso.");
    }

    @Override
    @Transactional(readOnly = true)
    public Response getMovimentacoes(Long id, int pagina, int tamanho) {
        validator.validarId(id);
        validator.validarPaginacao(pagina, tamanho);
        buscar(id, false);
        var resultado =
                movimentos.findByProdutoId(
                        id, PageRequest.of(pagina, tamanho, Sort.by("id").descending()));
        return Response.builder()
                .status(200)
                .movimentacoes(
                        resultado.getContent().stream()
                                .map(
                                        m ->
                                                new MovimentacaoResumoDTO(
                                                        m.getId(),
                                                        m.getTipo(),
                                                        m.getQuantidade(),
                                                        m.getSaldoAnterior(),
                                                        m.getSaldoPosterior(),
                                                        m.getChaveOperacao(),
                                                        m.getCriadoEm(),
                                                        m.getResponsavelId(),
                                                        m.getResponsavelNome(),
                                                        m.getMotivo()))
                                .toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(resultado.getTotalElements())
                .build();
    }

    private void vincular(Produto produto, Long categoriaId, Long fornecedorId) {
        var categoria =
                categorias
                        .findByIdForUpdate(categoriaId)
                        .orElseThrow(
                                () ->
                                        new ProdutoException(
                                                HttpStatus.NOT_FOUND, "Categoria nao encontrada."));
        if (!Boolean.TRUE.equals(categoria.getAtivo()))
            throw conflito("A categoria deve estar ativa.");
        var fornecedor =
                fornecedores
                        .findByIdForUpdate(fornecedorId)
                        .orElseThrow(
                                () ->
                                        new ProdutoException(
                                                HttpStatus.NOT_FOUND,
                                                "Fornecedor nao encontrado."));
        if (!Boolean.TRUE.equals(fornecedor.getAtivo()))
            throw conflito("O fornecedor deve estar ativo.");
        produto.setCategoria(categoria);
        produto.setFornecedor(fornecedor);
    }

    private void registrar(
            Produto produto,
            TipoMovimentacao tipo,
            int quantidade,
            int anterior,
            UUID chave,
            String motivo) {
        movimentos.saveAndFlush(
                MovimentacaoEstoque.builder()
                        .produto(produto)
                        .tipo(tipo)
                        .quantidade(quantidade)
                        .saldoAnterior(anterior)
                        .saldoPosterior(produto.getQuantidade())
                        .chaveOperacao(chave)
                        .criadoEm(LocalDateTime.now())
                        .responsavelNome("Nao identificado")
                        .motivo(motivo)
                        .build());
    }

    private Produto buscar(Long id, boolean bloquear) {
        return (bloquear ? repository.findByIdForUpdate(id) : repository.findById(id))
                .orElseThrow(
                        () ->
                                new ProdutoException(
                                        HttpStatus.NOT_FOUND, "Produto nao encontrado."));
    }

    private void validarAtivo(Produto produto) {
        if (!Boolean.TRUE.equals(produto.getAtivo()))
            throw conflito("Produto arquivado ou com status inconsistente.");
    }

    private void validarSaldo(Produto produto) {
        if (produto.getQuantidade() == null
                || produto.getQuantidade() < 0
                || produto.getQuantidade() > 1000000000) {
            throw conflito("Produto com saldo inconsistente.");
        }
    }

    private void validarVencimentoNovo(LocalDateTime vencimento) {
        if (vencimento != null && !vencimento.isAfter(LocalDateTime.now())) {
            throw new ProdutoException(
                    HttpStatus.BAD_REQUEST, "A validade informada deve ser futura.");
        }
    }

    private ProdutoResumoDTO resumo(Produto p) {
        return new ProdutoResumoDTO(
                p.getId(),
                p.getNome(),
                p.getSku(),
                p.getQuantidade(),
                p.getCategoria().getId(),
                p.getCategoria().getNome(),
                p.getFornecedor().getId(),
                p.getFornecedor().getNome(),
                p.getLocalArmazenamento(),
                p.getDescricao(),
                p.getVencimentoProduto(),
                p.getCriadoEm(),
                p.getAtivo(),
                p.getQuantidadeMinima());
    }

    private Response resposta(Produto produto, HttpStatus status, String mensagem) {
        return Response.builder()
                .status(status.value())
                .mensagem(mensagem)
                .produto(resumo(produto))
                .build();
    }

    private ProdutoException conflito(String mensagem) {
        return new ProdutoException(HttpStatus.CONFLICT, mensagem);
    }
}
