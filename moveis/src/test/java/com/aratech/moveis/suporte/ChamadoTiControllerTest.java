package com.aratech.moveis.suporte;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.suporte.chamados.controller.ChamadoTiController;
import com.aratech.moveis.suporte.chamados.controller.ChamadoTiExceptionHandler;
import com.aratech.moveis.suporte.chamados.dto.Response;
import com.aratech.moveis.suporte.chamados.service.ChamadoTiService;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

class ChamadoTiControllerTest {
    final ChamadoTiService service = mock(ChamadoTiService.class);
    final org.springframework.test.web.servlet.MockMvc mvc =
            MockMvcBuilders.standaloneSetup(new ChamadoTiController(service))
                    .setControllerAdvice(new ChamadoTiExceptionHandler())
                    .build();
    final String url = "/aratech/suporte/chamados";

    @Test
    void aberturaEConsultaUsamEndpointsReais() throws Exception {
        when(service.abrir(any()))
                .thenReturn(Response.builder().status(201).mensagem("Registrado").build());
        mvc.perform(
                        post(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"tipo\":\"Hardware\",\"chaveOperacao\":\""
                                                + UUID.randomUUID()
                                                + "\"}"))
                .andExpect(status().isCreated());
        when(service.listar(1, 8, "Aberto", "Monitor"))
                .thenReturn(Response.builder().status(200).chamados(List.of()).total(0L).build());
        mvc.perform(
                        get(url).param("pagina", "1")
                                .param("status", "Aberto")
                                .param("busca", "Monitor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chamados").isArray());
        verify(service).listar(1, 8, "Aberto", "Monitor");
    }

    @Test
    void rejeitaIdEJsonInvalidos() throws Exception {
        mvc.perform(get(url + "/abc")).andExpect(status().isBadRequest());
        mvc.perform(
                        post(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"chaveOperacao\":\"invalida\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
