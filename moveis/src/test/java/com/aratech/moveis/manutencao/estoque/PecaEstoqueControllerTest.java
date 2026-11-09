package com.aratech.moveis.manutencao.estoque;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.manutencao.estoque.controller.PecaEstoqueController;
import com.aratech.moveis.manutencao.estoque.controller.PecaEstoqueExceptionHandler;
import com.aratech.moveis.manutencao.estoque.dto.Response;
import com.aratech.moveis.manutencao.estoque.exception.PecaEstoqueException;
import com.aratech.moveis.manutencao.estoque.service.PecaEstoqueService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class PecaEstoqueControllerTest {
    final PecaEstoqueService service = mock(PecaEstoqueService.class);
    MockMvc mvc;
    final String base = "/aratech/manutencao/estoque";

    @BeforeEach
    void setup() {
        mvc =
                MockMvcBuilders.standaloneSetup(new PecaEstoqueController(service))
                        .setControllerAdvice(new PecaEstoqueExceptionHandler())
                        .build();
    }

    @Test
    void listaBuscaEHistoricoUsamRotasDeManutencao() throws Exception {
        when(service.listar(1, 8, "Rolamento"))
                .thenReturn(
                        Response.builder()
                                .status(200)
                                .pecas(List.of())
                                .pagina(1)
                                .tamanho(8)
                                .total(0L)
                                .build());
        mvc.perform(get(base).param("pagina", "1").param("busca", "Rolamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pecas").isArray());
        mvc.perform(get(base + "/42/movimentacoes")).andExpect(status().isOk());
        verify(service).historico(42L, 0, 8);
    }

    @Test
    void cadastroRetorna201EEdicaoRejeitaTentativaDeAlterarSaldo() throws Exception {
        when(service.adicionar(any())).thenReturn(Response.builder().status(201).build());
        mvc.perform(
                        post(base)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nome\":\"Rolamento\",\"quantidade\":1.5}"))
                .andExpect(status().isCreated());
        mvc.perform(
                        put(base + "/42")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantidade\":100}"))
                .andExpect(status().isBadRequest());
        verify(service, never()).atualizar(anyLong(), any());
    }

    @Test
    void conflitoDeSaldoPreservaMensagemENaoExpoeFalhas() throws Exception {
        when(service.saida(eq(42L), any()))
                .thenThrow(
                        new PecaEstoqueException(
                                HttpStatus.CONFLICT, "Quantidade maior que o estoque disponivel."));
        mvc.perform(
                        post(base + "/42/saidas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"quantidade\":2,\"motivo\":\"Reparo\",\"chaveOperacao\":\"11111111-2222-4333-8444-555555555555\"}"))
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.mensagem").value("Quantidade maior que o estoque disponivel."));
    }
}
