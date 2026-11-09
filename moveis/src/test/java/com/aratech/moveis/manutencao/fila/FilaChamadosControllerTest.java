package com.aratech.moveis.manutencao.fila;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.manutencao.chamados.controller.ChamadoExceptionHandler;
import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.fila.controller.FilaChamadosController;
import com.aratech.moveis.manutencao.fila.dto.AtribuicaoDTO;
import com.aratech.moveis.manutencao.fila.service.FilaChamadosService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class FilaChamadosControllerTest {
    final FilaChamadosService service = mock(FilaChamadosService.class);
    MockMvc mvc;
    final String base = "/aratech/manutencao/fila";

    @BeforeEach
    void setup() {
        mvc =
                MockMvcBuilders.standaloneSetup(new FilaChamadosController(service))
                        .setControllerAdvice(new ChamadoExceptionHandler())
                        .build();
    }

    @Test
    void consultaEFiltraNoBackend() throws Exception {
        when(service.listar(1, 8, "ABERTA", "Rolamento"))
                .thenReturn(
                        Response.builder()
                                .status(200)
                                .itens(List.of())
                                .pagina(1)
                                .tamanho(8)
                                .total(0L)
                                .build());
        mvc.perform(
                        get(base)
                                .param("pagina", "1")
                                .param("status", "ABERTA")
                                .param("busca", "Rolamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray());
        mvc.perform(
                        post(base + "/42/mecanico")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"mecanicoId\":7}"))
                .andExpect(status().isOk());
        verify(service).atribuir(42L, new AtribuicaoDTO(7L));
        mvc.perform(post(base + "/42/concluir")).andExpect(status().isOk());
        verify(service).concluir(42L);
    }

    @Test
    void conflitoMantemMensagemEIdInvalidoRetorna400() throws Exception {
        when(service.concluir(42L))
                .thenThrow(
                        new ChamadoException(HttpStatus.CONFLICT, "Atribua um mecanico primeiro."));
        mvc.perform(post(base + "/42/concluir"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Atribua um mecanico primeiro."));
        mvc.perform(get(base + "/invalido")).andExpect(status().isBadRequest());
    }
}
