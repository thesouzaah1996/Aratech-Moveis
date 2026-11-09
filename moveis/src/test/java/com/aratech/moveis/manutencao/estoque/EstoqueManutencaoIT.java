package com.aratech.moveis.manutencao.estoque;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.manutencao.estoque.dto.MovimentacaoPecaDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueDTO;
import com.aratech.moveis.manutencao.estoque.exception.PecaEstoqueException;
import com.aratech.moveis.manutencao.estoque.service.PecaEstoqueService;

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
class EstoqueManutencaoIT {
    @Autowired PecaEstoqueService service;
    @Autowired JdbcTemplate jdbc;

    PecaEstoqueDTO cadastro(String codigo, String saldo) {
        return PecaEstoqueDTO.builder()
                .nome("Lubrificante " + codigo)
                .codigo(codigo)
                .quantidade(new BigDecimal(saldo))
                .unidade("L")
                .localizacao("Prateleira Teste")
                .descricao("Teste transacional")
                .build();
    }

    @Test
    @Transactional
    void cadastroMovimentacaoReenvioHistoricoEArquivo() {
        String codigo = "MNT-IT-" + UUID.randomUUID();
        var p = service.adicionar(cadastro(codigo, "1.25")).getPeca();
        var entrada =
                new MovimentacaoPecaDTO(
                        new BigDecimal("0.75"), UUID.randomUUID(), "Recebimento de compra");
        service.entrada(p.id(), entrada);
        service.entrada(p.id(), entrada);
        assertEquals(
                0, new BigDecimal("2").compareTo(service.buscar(p.id()).getPeca().quantidade()));
        service.saida(
                p.id(),
                new MovimentacaoPecaDTO(
                        new BigDecimal("2"), UUID.randomUUID(), "Reparo do compressor"));
        assertEquals(3L, service.historico(p.id(), 0, 8).getTotal());
        assertFalse(service.arquivar(p.id()).getPeca().ativo());
        assertEquals(0L, service.listar(0, 8, codigo).getTotal());
        assertEquals(3L, service.historico(p.id(), 0, 8).getTotal());
    }

    @Test
    void duasSaidasConcorrentesNaoNegativamSaldo() throws Exception {
        var p = service.adicionar(cadastro("MNT-IT-" + UUID.randomUUID(), "10")).getPeca();
        var executor = Executors.newFixedThreadPool(2);
        var inicio = new CountDownLatch(1);
        Callable<Boolean> retirar =
                () -> {
                    inicio.await();
                    try {
                        service.saida(
                                p.id(),
                                new MovimentacaoPecaDTO(
                                        new BigDecimal("7"),
                                        UUID.randomUUID(),
                                        "Retirada concorrente"));
                        return true;
                    } catch (PecaEstoqueException e) {
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
            jdbc.update("DELETE FROM manutencao_movimentacao_estoque WHERE peca_id=?", p.id());
            jdbc.update("DELETE FROM manutencao_peca_estoque WHERE id=?", p.id());
        }
    }
}
