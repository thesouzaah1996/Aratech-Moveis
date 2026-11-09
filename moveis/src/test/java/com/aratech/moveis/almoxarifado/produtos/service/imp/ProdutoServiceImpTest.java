package com.aratech.moveis.almoxarifado.produtos.service.imp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aratech.moveis.almoxarifado.categorias.model.Categoria;
import com.aratech.moveis.almoxarifado.categorias.repository.CategoriaRepository;
import com.aratech.moveis.almoxarifado.fornecedores.model.Fornecedor;
import com.aratech.moveis.almoxarifado.fornecedores.repository.FornecedorRepository;
import com.aratech.moveis.almoxarifado.produtos.dto.MovimentacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.ProdutoAtualizacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.ProdutoDTO;
import com.aratech.moveis.almoxarifado.produtos.enums.TipoMovimentacao;
import com.aratech.moveis.almoxarifado.produtos.exception.ProdutoException;
import com.aratech.moveis.almoxarifado.produtos.model.MovimentacaoEstoque;
import com.aratech.moveis.almoxarifado.produtos.model.Produto;
import com.aratech.moveis.almoxarifado.produtos.repository.MovimentacaoEstoqueRepository;
import com.aratech.moveis.almoxarifado.produtos.repository.ProdutoRepository;
import com.aratech.moveis.almoxarifado.produtos.validation.ProdutoValidator;
import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;

import jakarta.validation.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

class ProdutoServiceImpTest {
    private static final ValidatorFactory VALIDATORS = Validation.buildDefaultValidatorFactory();
    private final ProdutoRepository repository = mock(ProdutoRepository.class);
    private final MovimentacaoEstoqueRepository movimentos =
            mock(MovimentacaoEstoqueRepository.class);
    private final CategoriaRepository categorias = mock(CategoriaRepository.class);
    private final FornecedorRepository fornecedores = mock(FornecedorRepository.class);
    private final ProdutoServiceImp service =
            new ProdutoServiceImp(
                    repository,
                    movimentos,
                    categorias,
                    fornecedores,
                    new ProdutoValidator(VALIDATORS.getValidator()),
                    mock(PedidoCompraService.class));
    private final Categoria categoria = Categoria.builder().id(1L).nome("Ferragens").build();
    private final Fornecedor fornecedor =
            Fornecedor.builder().id(2L).nome("Fornecedor Exemplo").build();

    @AfterAll
    static void fechar() {
        VALIDATORS.close();
    }

    @BeforeEach
    void configurar() {
        when(categorias.findByIdForUpdate(1L)).thenReturn(Optional.of(categoria));
        when(fornecedores.findByIdForUpdate(2L)).thenReturn(Optional.of(fornecedor));
        when(repository.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            Produto p = i.getArgument(0);
                            if (p.getId() == null) p.setId(1L);
                            return p;
                        });
    }

    private ProdutoDTO cadastro() {
        return ProdutoDTO.builder()
                .nome("Parafuso")
                .sku("prd-1")
                .categoriaID(1L)
                .fornecedorID(2L)
                .quantidade(BigDecimal.TEN)
                .localArmazenamento("Prateleira A1")
                .build();
    }

    private ProdutoAtualizacaoDTO edicao() {
        return ProdutoAtualizacaoDTO.builder()
                .nome("Parafuso atualizado")
                .sku("PRD-1")
                .categoriaID(1L)
                .fornecedorID(2L)
                .localArmazenamento("Prateleira B1")
                .build();
    }

    private Produto existente(int quantidade) {
        var p =
                Produto.builder()
                        .id(1L)
                        .nome("Parafuso")
                        .sku("PRD-1")
                        .quantidade(quantidade)
                        .categoria(categoria)
                        .fornecedor(fornecedor)
                        .localArmazenamento("Prateleira A1")
                        .build();
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        return p;
    }

    private MovimentacaoDTO movimento(int quantidade) {
        return new MovimentacaoDTO(
                BigDecimal.valueOf(quantidade), UUID.randomUUID(), "Conferencia de estoque");
    }

    @Test
    void cadastroCriaSaldoInicialNormalizaSkuEGravaHistorico() {
        var resposta = service.addProduto(cadastro());
        assertEquals(201, resposta.getStatus());
        assertEquals("PRD-1", resposta.getProduto().sku());
        assertEquals(10, resposta.getProduto().quantidade());
        var captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentos).saveAndFlush(captor.capture());
        assertEquals(TipoMovimentacao.INICIAL, captor.getValue().getTipo());
        assertEquals(0, captor.getValue().getSaldoAnterior());
        assertNull(captor.getValue().getResponsavelId());
        assertEquals("Nao identificado", captor.getValue().getResponsavelNome());
        assertEquals("Saldo inicial do cadastro", captor.getValue().getMotivo());
        assertEquals(10, captor.getValue().getSaldoPosterior());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "1.5", "1000000001"})
    void cadastroRejeitaSaldoNegativoFracionadoOuExcessivo(String valor) {
        var dto = cadastro();
        dto.setQuantidade(new BigDecimal(valor));
        assertThrows(ConstraintViolationException.class, () -> service.addProduto(dto));
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(movimentos);
    }

    @Test
    void cadastroAceitaSaldoZero() {
        var dto = cadastro();
        dto.setQuantidade(BigDecimal.ZERO);
        assertEquals(0, service.addProduto(dto).getProduto().quantidade());
    }

    @Test
    void validaCamposObrigatoriosMesmoSemController() {
        assertThrows(ProdutoException.class, () -> service.addProduto(null));
        var dto = cadastro();
        dto.setNome(" ");
        dto.setCategoriaID(null);
        dto.setFornecedorID(null);
        dto.setLocalArmazenamento(" ");
        assertThrows(ConstraintViolationException.class, () -> service.addProduto(dto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void bloqueiaSkuDuplicadoIncluindoArquivados() {
        when(repository.existsBySkuIgnoreCase("PRD-1")).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(ProdutoException.class, () -> service.addProduto(cadastro()))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void bloqueiaCategoriaOuFornecedorInativos() {
        categoria.setAtivo(false);
        assertThrows(ProdutoException.class, () -> service.addProduto(cadastro()));
        categoria.setAtivo(true);
        fornecedor.setAtivo(false);
        assertThrows(ProdutoException.class, () -> service.addProduto(cadastro()));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void bloqueiaReferenciasInexistentes() {
        when(categorias.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(ProdutoException.class, () -> service.addProduto(cadastro()))
                        .getStatus());
        when(categorias.findByIdForUpdate(1L)).thenReturn(Optional.of(categoria));
        when(fornecedores.findByIdForUpdate(2L)).thenReturn(Optional.empty());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(ProdutoException.class, () -> service.addProduto(cadastro()))
                        .getStatus());
    }

    @Test
    void rejeitaNovoVencimentoNoPassado() {
        var dto = cadastro();
        dto.setVencimentoProduto(LocalDateTime.now().minusDays(1));
        assertThrows(ProdutoException.class, () -> service.addProduto(dto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void atualizaDadosSemModificarSaldoSkuOuDataCriacao() {
        var p = existente(42);
        p.setCriadoEm(LocalDateTime.of(2026, 1, 1, 10, 0));
        service.updateProduto(edicao(), 1L);
        assertEquals(42, p.getQuantidade());
        assertEquals("PRD-1", p.getSku());
        assertEquals("Parafuso atualizado", p.getNome());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), p.getCriadoEm());
        verifyNoInteractions(movimentos);
    }

    @Test
    void bloqueiaTrocaDeSku() {
        existente(10);
        var dto = edicao();
        dto.setSku("OUTRO");
        assertThrows(ProdutoException.class, () -> service.updateProduto(dto, 1L));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void entradaESaidaCalculamSaldoComBloqueioNoServidor() {
        var p = existente(10);
        assertEquals(15, service.entrada(1L, movimento(5)).getProduto().quantidade());
        assertEquals(8, service.saida(1L, movimento(7)).getProduto().quantidade());
        assertEquals(8, p.getQuantidade());
        verify(repository, times(2)).findByIdForUpdate(1L);
        verify(repository, never()).findById(any());
        verify(movimentos, times(2)).saveAndFlush(any());
    }

    @Test
    void saidaNaoPermiteSaldoNegativo() {
        var p = existente(10);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(ProdutoException.class, () -> service.saida(1L, movimento(11)))
                        .getStatus());
        assertEquals(10, p.getQuantidade());
        verify(repository, never()).saveAndFlush(any());
        verify(movimentos, never()).saveAndFlush(any());
    }

    @Test
    void saidaPodeZerarSaldo() {
        existente(10);
        assertEquals(0, service.saida(1L, movimento(10)).getProduto().quantidade());
    }

    @Test
    void entradaNaoPermiteExcederLimite() {
        var p = existente(1000000000);
        assertThrows(ProdutoException.class, () -> service.entrada(1L, movimento(1)));
        assertEquals(1000000000, p.getQuantidade());
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.5", "1000000001"})
    void movimentoRejeitaQuantidadeInvalida(String valor) {
        var dto = movimento(1);
        dto.setQuantidade(new BigDecimal(valor));
        assertThrows(ConstraintViolationException.class, () -> service.entrada(1L, dto));
        assertThrows(ConstraintViolationException.class, () -> service.saida(1L, dto));
        verify(repository, never()).findByIdForUpdate(any());
    }

    @Test
    void exigeChaveParaIdempotencia() {
        var dto = movimento(1);
        dto.setChaveOperacao(null);
        assertThrows(ConstraintViolationException.class, () -> service.entrada(1L, dto));
    }

    @Test
    void repetirOperacaoNaoAlteraSaldoNovamente() {
        var p = existente(10);
        var dto = movimento(5);
        when(movimentos.findByChaveOperacao(dto.getChaveOperacao()))
                .thenReturn(
                        Optional.of(
                                MovimentacaoEstoque.builder()
                                        .produto(p)
                                        .tipo(TipoMovimentacao.ENTRADA)
                                        .quantidade(5)
                                        .responsavelId(7L)
                                        .motivo("Conferencia de estoque")
                                        .build()));
        assertEquals(10, service.entrada(1L, dto).getProduto().quantidade());
        verify(repository, never()).saveAndFlush(any());
        verify(movimentos, never()).saveAndFlush(any());
    }

    @Test
    void chaveNaoPodeSerReutilizadaComOutroTipoOuQuantidade() {
        var p = existente(10);
        var dto = movimento(5);
        when(movimentos.findByChaveOperacao(dto.getChaveOperacao()))
                .thenReturn(
                        Optional.of(
                                MovimentacaoEstoque.builder()
                                        .produto(p)
                                        .tipo(TipoMovimentacao.ENTRADA)
                                        .quantidade(5)
                                        .responsavelId(7L)
                                        .motivo("Conferencia de estoque")
                                        .build()));
        assertThrows(ProdutoException.class, () -> service.saida(1L, dto));
        dto.setQuantidade(BigDecimal.ONE);
        assertThrows(ProdutoException.class, () -> service.entrada(1L, dto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void bloqueiaEntradaDeVencidoMasPermiteRetiradaParaBaixa() {
        var p = existente(10);
        p.setVencimentoProduto(LocalDateTime.now().minusDays(1));
        assertThrows(ProdutoException.class, () -> service.entrada(1L, movimento(1)));
        assertEquals(9, service.saida(1L, movimento(1)).getProduto().quantidade());
    }

    @Test
    void fornecedorInativoImpedeEntradaMasNaoRetiradaDoSaldoExistente() {
        existente(10);
        fornecedor.setAtivo(false);
        assertThrows(ProdutoException.class, () -> service.entrada(1L, movimento(1)));
        assertEquals(9, service.saida(1L, movimento(1)).getProduto().quantidade());
    }

    @Test
    void arquivaSomenteComSaldoZeroPreservandoHistorico() {
        var p = existente(10);
        assertThrows(ProdutoException.class, () -> service.deleteProduto(1L));
        p.setQuantidade(0);
        service.deleteProduto(1L);
        assertFalse(p.getAtivo());
        service.deleteProduto(1L);
        verify(repository, times(1)).saveAndFlush(p);
        verify(repository, never()).delete(any());
        verifyNoInteractions(movimentos);
    }

    @Test
    void arquivadoNaoRecebeAtualizacaoOuNovasMovimentacoes() {
        var p = existente(0);
        p.setAtivo(false);
        assertThrows(ProdutoException.class, () -> service.updateProduto(edicao(), 1L));
        assertThrows(ProdutoException.class, () -> service.entrada(1L, movimento(1)));
        assertThrows(ProdutoException.class, () -> service.saida(1L, movimento(1)));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void consultaEListaPaginadaUsamResumoComNomesDosVinculos() {
        var p = existente(10);
        when(repository.buscar(eq("Parafuso"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));
        var resposta = service.getAllProdutos(0, 8, "Parafuso");
        assertEquals(1L, resposta.getTotal());
        assertEquals("Ferragens", resposta.getProdutos().getFirst().categoriaNome());
        assertEquals(
                "Fornecedor Exemplo", service.getProdutoById(1L).getProduto().fornecedorNome());
    }

    @Test
    void historicoRetornaDadosPaginadosMesmoSeArquivado() {
        var p = existente(0);
        p.setAtivo(false);
        var m =
                MovimentacaoEstoque.builder()
                        .id(1L)
                        .produto(p)
                        .tipo(TipoMovimentacao.INICIAL)
                        .quantidade(0)
                        .saldoAnterior(0)
                        .saldoPosterior(0)
                        .chaveOperacao(UUID.randomUUID())
                        .criadoEm(LocalDateTime.now())
                        .build();
        when(movimentos.findByProdutoId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(m)));
        assertEquals(1, service.getMovimentacoes(1L, 0, 8).getMovimentacoes().size());
    }

    @Test
    void validaIdsEPaginacao() {
        for (Long id : new Long[] {null, 0L, -1L}) {
            assertThrows(ProdutoException.class, () -> service.getProdutoById(id));
            assertThrows(ProdutoException.class, () -> service.updateProduto(edicao(), id));
            assertThrows(ProdutoException.class, () -> service.entrada(id, movimento(1)));
            assertThrows(ProdutoException.class, () -> service.saida(id, movimento(1)));
            assertThrows(ProdutoException.class, () -> service.deleteProduto(id));
            assertThrows(ProdutoException.class, () -> service.getMovimentacoes(id, 0, 8));
        }
        assertThrows(ProdutoException.class, () -> service.getAllProdutos(-1, 8, null));
        assertThrows(ProdutoException.class, () -> service.getAllProdutos(0, 101, null));
    }

    @Test
    void inexistenteRetorna404() {
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(ProdutoException.class, () -> service.getProdutoById(99L))
                        .getStatus());
        assertThrows(ProdutoException.class, () -> service.saida(99L, movimento(1)));
    }

    @Test
    void motivoObrigatorioEAutorNaoEInformadoPeloCliente() {
        var dto = movimento(1);
        dto.setMotivo(" ");
        assertThrows(ConstraintViolationException.class, () -> service.entrada(1L, dto));
        verify(repository, never()).saveAndFlush(any());
    }
}
