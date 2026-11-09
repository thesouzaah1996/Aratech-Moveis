package com.aratech.moveis.operacional;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.financeiro.titulos.dto.NovoTitulo;
import com.aratech.moveis.financeiro.titulos.dto.Titulo;
import com.aratech.moveis.financeiro.titulos.service.FinanceiroService;
import com.aratech.moveis.security.dto.UsuarioSessao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

@SpringBootTest
@ActiveProfiles("dev")
class OperacaoConcorrenciaIT {
    @Autowired FinanceiroService financeiro;
    @Autowired JdbcTemplate jdbc;

    @Test
    void doisEnviosSimultaneosDaMesmaChaveCriamUmaObrigacao() throws Exception {
        UUID chave = UUID.randomUUID();
        String descricao = "TESTE-CONCORRENCIA-" + chave;
        var dto =
                new NovoTitulo(
                        descricao,
                        "Fornecedor de teste",
                        new BigDecimal("10.00"),
                        LocalDate.now(),
                        null,
                        null,
                        null,
                        chave,
                        0);
        var largada = new CountDownLatch(1);
        Callable<Titulo> operacao =
                () -> {
                    var contexto = SecurityContextHolder.createEmptyContext();
                    contexto.setAuthentication(
                            UsernamePasswordAuthenticationToken.authenticated(
                                    new UsuarioSessao(
                                            0L,
                                            "Teste concorrência",
                                            "teste@example.test",
                                            true,
                                            0,
                                            Instant.now(),
                                            Set.of(),
                                            Set.of()),
                                    null,
                                    List.of()));
                    SecurityContextHolder.setContext(contexto);
                    try {
                        assertTrue(largada.await(20, TimeUnit.SECONDS));
                        return financeiro.salvar("pagar", null, dto);
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                };
        try (var executor = Executors.newFixedThreadPool(2)) {
            var a = executor.submit(operacao);
            var b = executor.submit(operacao);
            largada.countDown();
            assertEquals(a.get(30, TimeUnit.SECONDS).id(), b.get(30, TimeUnit.SECONDS).id());
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "SELECT count(*) FROM financeiro_titulo WHERE descricao=?",
                            Integer.class,
                            descricao));
        } finally {
            for (Long id :
                    jdbc.queryForList(
                            "SELECT id FROM financeiro_titulo WHERE descricao=?",
                            Long.class,
                            descricao)) {
                jdbc.update(
                        "DELETE FROM gestao_auditoria WHERE entidade='TITULO' AND registro_id=?",
                        id);
                jdbc.update("DELETE FROM financeiro_titulo WHERE id=?", id);
            }
            jdbc.update("DELETE FROM gestao_comando WHERE chave=?", chave);
        }
    }
}
