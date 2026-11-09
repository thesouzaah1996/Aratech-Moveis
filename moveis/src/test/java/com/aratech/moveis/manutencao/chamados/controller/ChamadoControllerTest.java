package com.aratech.moveis.manutencao.chamados.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.manutencao.chamados.dto.ChamadoResumoDTO;
import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.enums.TipoManutencao;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.service.ChamadoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

class ChamadoControllerTest {
    final ChamadoService service = mock(ChamadoService.class);
    final String base = "/aratech/manutencao/chamados";
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc =
                MockMvcBuilders.standaloneSetup(new ChamadoController(service))
                        .setControllerAdvice(new ChamadoExceptionHandler())
                        .build();
    }

    @Test
    void cadastraJsonRetorna201NumeroEData() throws Exception {
        var c =
                new ChamadoResumoDTO(
                        42L,
                        "Prensa",
                        TipoManutencao.CORRETIVA,
                        PrioridadeChamado.ALTA,
                        "Ana Silva",
                        "Producao",
                        "",
                        "Motor parado",
                        StatusChamado.ABERTA,
                        Instant.parse("2026-11-01T10:00:00Z"));
        when(service.solicitar(any()))
                .thenReturn(Response.builder().status(201).chamado(c).build());
        mvc.perform(
                        post(base)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"equipamento\":\"Prensa\",\"tipo\":\"CORRETIVA\",\"prioridade\":\"ALTA\",\"solicitante\":\"Ana"
                                            + " Silva\",\"setor\":\"Producao\",\"telefone\":\"\",\"descricao\":\"Motor"
                                            + " parado\",\"chaveOperacao\":\"11111111-2222-4333-8444-555555555555\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chamado.id").value(42))
                .andExpect(jsonPath("$.chamado.status").value("ABERTA"))
                .andExpect(jsonPath("$.chamado.dataAbertura").exists());
    }

    @Test
    void jsonOuChaveInvalidosRetornam400() throws Exception {
        mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post(base)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"chaveOperacao\":\"invalida\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void validaCampoEConflitoComMensagemPadrao() throws Exception {
        when(service.solicitar(any()))
                .thenThrow(new ChamadoException(HttpStatus.BAD_REQUEST, "Informe o equipamento."));
        mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Informe o equipamento."));
        doThrow(new ChamadoException(HttpStatus.CONFLICT, "Chave ja utilizada."))
                .when(service)
                .solicitar(any());
        mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getPaginadoEPorId() throws Exception {
        when(service.listar(2, 8, "ABERTA"))
                .thenReturn(
                        Response.builder()
                                .status(200)
                                .chamados(List.of())
                                .pagina(2)
                                .tamanho(8)
                                .total(0L)
                                .build());
        mvc.perform(get(base).param("pagina", "2").param("tamanho", "8").param("status", "ABERTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chamados").isArray());
        when(service.buscar(99L))
                .thenThrow(
                        new ChamadoException(HttpStatus.NOT_FOUND, "Solicitacao nao encontrada."));
        mvc.perform(get(base + "/99")).andExpect(status().isNotFound());
        mvc.perform(get(base + "/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void falhaInesperadaNaoExpoeDetalhesInternos() throws Exception {
        when(service.listar(0, 8, null)).thenThrow(new RuntimeException("senha secreta e SQL"));
        mvc.perform(get(base))
                .andExpect(status().isInternalServerError())
                .andExpect(
                        jsonPath("$.mensagem")
                                .value("Nao foi possivel concluir a operacao. Tente novamente."));
    }
}
