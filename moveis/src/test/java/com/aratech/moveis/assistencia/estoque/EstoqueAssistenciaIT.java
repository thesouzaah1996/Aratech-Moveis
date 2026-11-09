package com.aratech.moveis.assistencia.estoque;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.assistencia.estoque.dto.MovimentoAssistenciaDTO;
import com.aratech.moveis.assistencia.estoque.dto.PecaAssistenciaAtualizacaoDTO;
import com.aratech.moveis.assistencia.estoque.dto.PecaAssistenciaDTO;
import com.aratech.moveis.assistencia.estoque.exception.PecaAssistenciaException;
import com.aratech.moveis.assistencia.estoque.service.PecaAssistenciaService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class EstoqueAssistenciaIT {
    @Autowired PecaAssistenciaService service;
    @Autowired JdbcTemplate jdbc;

    PecaAssistenciaDTO cadastro(String codigo, String saldo) {
        return PecaAssistenciaDTO.builder()
                .nome("Porta " + codigo)
                .codigo(codigo)
                .modelo("Milano")
                .quantidadeMinima(new BigDecimal("2"))
                .quantidade(new BigDecimal(saldo))
                .unidade("un")
                .localizacao("Prateleira Teste")
                .descricao("Teste transacional")
                .build();
    }

    @Test
    @Transactional
    void cadastroMovimentacaoReenvioHistoricoEArquivo() {
        String codigo = "AST-IT-" + UUID.randomUUID();
        var p = service.adicionar(cadastro(codigo, "1.25")).getPeca();
        var entrada =
                new MovimentoAssistenciaDTO(
                        new BigDecimal("0.75"), UUID.randomUUID(), "Recebimento de compra");
        service.entrada(p.id(), entrada);
        service.entrada(p.id(), entrada);
        assertEquals(
                0, new BigDecimal("2").compareTo(service.buscar(p.id()).getPeca().quantidade()));
        service.saida(
                p.id(),
                new MovimentoAssistenciaDTO(
                        new BigDecimal("2"), UUID.randomUUID(), "Troca da porta do guarda-roupa"));
        assertEquals(3L, service.historico(p.id(), 0, 8).getTotal());
        assertFalse(service.arquivar(p.id()).getPeca().ativo());
        assertEquals(0L, service.listar(0, 8, codigo, false).getTotal());
        assertEquals(3L, service.historico(p.id(), 0, 8).getTotal());
    }

    @Test
    void duasSaidasConcorrentesNaoNegativamSaldo() throws Exception {
        var p = service.adicionar(cadastro("AST-IT-" + UUID.randomUUID(), "10")).getPeca();
        var executor = Executors.newFixedThreadPool(2);
        var inicio = new CountDownLatch(1);
        Callable<Boolean> retirar =
                () -> {
                    inicio.await();
                    try {
                        service.saida(
                                p.id(),
                                new MovimentoAssistenciaDTO(
                                        new BigDecimal("7"),
                                        UUID.randomUUID(),
                                        "Retirada concorrente"));
                        return true;
                    } catch (PecaAssistenciaException e) {
                        assertEquals(HttpStatus.CONFLICT, e.getStatus());
                        return false;
                    }
                };
        try {
            var a = executor.submit(retirar);
            var b = executor.submit(retirar);
            inicio.countDown();
            int sucessos =
                    (a.get(30, TimeUnit.SECONDS) ? 1 : 0) + (b.get(30, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, sucessos);
            assertEquals(
                    0,
                    new BigDecimal("3").compareTo(service.buscar(p.id()).getPeca().quantidade()));
            assertEquals(2L, service.historico(p.id(), 0, 8).getTotal());
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            jdbc.update("DELETE FROM assistencia_movimentacao_estoque WHERE peca_id=?", p.id());
            jdbc.update("DELETE FROM assistencia_peca_estoque WHERE id=?", p.id());
        }
    }

    @Test
    @Transactional
    void modeloMinimoBuscaEPaginacaoPersistem() {
        String codigo = "AST-IT-" + UUID.randomUUID();
        var p = service.adicionar(cadastro(codigo, "1")).getPeca();
        assertEquals("Milano", p.modelo());
        assertEquals(0, new BigDecimal("2").compareTo(p.quantidadeMinima()));
        var dto =
                PecaAssistenciaAtualizacaoDTO.builder()
                        .nome("Gaveta")
                        .codigo(codigo)
                        .modelo("Verona " + codigo)
                        .quantidadeMinima(new BigDecimal("3"))
                        .unidade("un")
                        .localizacao("Prateleira B")
                        .descricao("Frente da gaveta")
                        .build();
        var editada = service.atualizar(p.id(), dto).getPeca();
        assertEquals("Verona " + codigo, editada.modelo());
        assertEquals("Gaveta", editada.nome());
        assertEquals(0, BigDecimal.ONE.compareTo(editada.quantidade()));
        assertEquals(1L, service.listar(0, 1, editada.modelo(), true).getTotal());
        assertTrue(service.listar(0, 1, codigo, true).getTotalAbaixoMinimo() > 0);
        assertEquals(0L, service.listar(0, 8, codigo + "%", false).getTotal());
        assertEquals(0L, service.listar(0, 8, codigo + "_", false).getTotal());
        assertThrows(PecaAssistenciaException.class, () -> service.arquivar(p.id()));
    }

    @Test
    @Transactional
    void rejeitaModeloOuMinimoInvalido() {
        String codigo = "AST-IT-" + UUID.randomUUID();
        var modeloInvalido = cadastro(codigo, "0");
        modeloInvalido.setModelo(" ");
        assertThrows(
                jakarta.validation.ConstraintViolationException.class,
                () -> service.adicionar(modeloInvalido));
        var d = cadastro(codigo, "0");
        d.setQuantidadeMinima(new BigDecimal("-1"));
        final var minimoInvalido = d;
        assertThrows(
                jakarta.validation.ConstraintViolationException.class,
                () -> service.adicionar(minimoInvalido));
    }
}
