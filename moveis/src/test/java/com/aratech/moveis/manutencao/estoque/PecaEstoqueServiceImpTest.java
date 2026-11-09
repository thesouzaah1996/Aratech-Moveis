package com.aratech.moveis.manutencao.estoque;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;
import com.aratech.moveis.manutencao.estoque.dto.MovimentacaoPecaDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueAtualizacaoDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueDTO;
import com.aratech.moveis.manutencao.estoque.enums.TipoMovimentacaoPeca;
import com.aratech.moveis.manutencao.estoque.exception.PecaEstoqueException;
import com.aratech.moveis.manutencao.estoque.model.MovimentacaoPeca;
import com.aratech.moveis.manutencao.estoque.model.PecaEstoque;
import com.aratech.moveis.manutencao.estoque.repository.MovimentacaoPecaRepository;
import com.aratech.moveis.manutencao.estoque.repository.PecaEstoqueRepository;
import com.aratech.moveis.manutencao.estoque.service.imp.PecaEstoqueServiceImp;
import com.aratech.moveis.manutencao.estoque.validation.PecaEstoqueValidator;

import jakarta.validation.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

class PecaEstoqueServiceImpTest {
    static ValidatorFactory factory;
    final PecaEstoqueRepository repo = mock(PecaEstoqueRepository.class);
    final MovimentacaoPecaRepository movimentos = mock(MovimentacaoPecaRepository.class);
    PecaEstoqueServiceImp service;

    @BeforeAll
    static void abrir() {
        factory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void fechar() {
        factory.close();
    }

    @BeforeEach
    void setup() {
        service =
                new PecaEstoqueServiceImp(
                        repo,
                        movimentos,
                        new PecaEstoqueValidator(factory.getValidator()),
                        mock(PedidoCompraService.class));
        when(repo.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            PecaEstoque p = i.getArgument(0);
                            if (p.getId() == null) p.setId(42L);
                            return p;
                        });
    }

    PecaEstoqueDTO cadastro(String q) {
        return PecaEstoqueDTO.builder()
                .nome("Lubrificante")
                .codigo("mnt-01")
                .unidade("L")
                .localizacao("Prateleira A")
                .descricao("")
                .quantidade(new BigDecimal(q))
                .build();
    }

    PecaEstoque peca(String saldo) {
        var p =
                PecaEstoque.builder()
                        .id(42L)
                        .nome("Lubrificante")
                        .codigo("MNT-01")
                        .unidade("L")
                        .localizacao("Prateleira A")
                        .descricao("")
                        .quantidade(new BigDecimal(saldo))
                        .criadoEm(Instant.now())
                        .build();
        when(repo.findByIdForUpdate(42L)).thenReturn(Optional.of(p));
        when(repo.findById(42L)).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    void cadastroNormalizaCodigoERegistraSaldoInicial() {
        var r = service.adicionar(cadastro("1.25"));
        assertEquals("MNT-01", r.getPeca().codigo());
        assertNotNull(r.getPeca().criadoEm());
        var cap = org.mockito.ArgumentCaptor.forClass(MovimentacaoPeca.class);
        verify(movimentos).saveAndFlush(cap.capture());
        assertEquals(TipoMovimentacaoPeca.INICIAL, cap.getValue().getTipo());
        assertEquals(0, cap.getValue().getSaldoAnterior().signum());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0.0001", "1000000000000"})
    void rejeitaSaldoInvalido(String q) {
        assertThrows(ConstraintViolationException.class, () -> service.adicionar(cadastro(q)));
        verifyNoInteractions(repo, movimentos);
    }

    @Test
    void codigoDuplicadoIncluindoArquivadoEhRejeitado() {
        when(repo.existsByCodigoIgnoreCase("MNT-01")).thenReturn(true);
        assertThrows(PecaEstoqueException.class, () -> service.adicionar(cadastro("0")));
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void saidaNaoPodeNegativarSaldo() {
        var p = peca("1.5");
        assertThrows(
                PecaEstoqueException.class,
                () ->
                        service.saida(
                                42L,
                                new MovimentacaoPecaDTO(
                                        new BigDecimal("2"),
                                        UUID.randomUUID(),
                                        "Retirada para reparo")));
        assertEquals(0, new BigDecimal("1.5").compareTo(p.getQuantidade()));
        verify(movimentos, never()).saveAndFlush(any());
    }

    @Test
    void movimentacaoDecimalEhCalculadaNoServidorERepeticaoNaoDuplica() {
        var p = peca("1.5");
        var chave = UUID.randomUUID();
        var dto = new MovimentacaoPecaDTO(new BigDecimal("0.25"), chave, "Retirada para reparo");
        assertEquals(
                0,
                new BigDecimal("1.25").compareTo(service.saida(42L, dto).getPeca().quantidade()));
        var m =
                MovimentacaoPeca.builder()
                        .peca(p)
                        .tipo(TipoMovimentacaoPeca.SAIDA)
                        .quantidade(new BigDecimal("0.250"))
                        .chaveOperacao(chave)
                        .motivo(dto.getMotivo())
                        .build();
        when(movimentos.findByChaveOperacao(chave)).thenReturn(Optional.of(m));
        assertEquals(
                0,
                new BigDecimal("1.25").compareTo(service.saida(42L, dto).getPeca().quantidade()));
        verify(repo, times(1)).saveAndFlush(any());
        verify(movimentos, times(1)).saveAndFlush(any());
        assertThrows(PecaEstoqueException.class, () -> service.entrada(42L, dto));
    }

    @Test
    void editarNaoAlteraSaldoECodigoUnidadeSaoProtegidos() {
        var p = peca("1.5");
        service.atualizar(
                42L,
                new PecaEstoqueAtualizacaoDTO(
                        "Lubrificante novo", "MNT-01", "L", "Prateleira B", "Detalhes"));
        assertEquals(0, new BigDecimal("1.5").compareTo(p.getQuantidade()));
        assertThrows(
                PecaEstoqueException.class,
                () ->
                        service.atualizar(
                                42L,
                                new PecaEstoqueAtualizacaoDTO(
                                        "Lubrificante", "OUTRO", "L", "Prateleira A", "")));
        assertThrows(
                PecaEstoqueException.class,
                () ->
                        service.atualizar(
                                42L,
                                new PecaEstoqueAtualizacaoDTO(
                                        "Lubrificante", "MNT-01", "un", "Prateleira A", "")));
    }

    @Test
    void arquivamentoExigeSaldoZeroEPreservaHistorico() {
        var p = peca("1");
        assertThrows(PecaEstoqueException.class, () -> service.arquivar(42L));
        p.setQuantidade(BigDecimal.ZERO);
        assertFalse(service.arquivar(42L).getPeca().ativo());
        assertThrows(
                PecaEstoqueException.class,
                () ->
                        service.entrada(
                                42L,
                                new MovimentacaoPecaDTO(
                                        BigDecimal.ONE, UUID.randomUUID(), "Recebimento")));
        verify(repo, never()).delete(any());
    }

    @Test
    void listaPaginadaUsaTotalDoBanco() {
        when(repo.buscar(eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 8), 17));
        assertEquals(17L, service.listar(0, 8, "").getTotal());
    }
}
