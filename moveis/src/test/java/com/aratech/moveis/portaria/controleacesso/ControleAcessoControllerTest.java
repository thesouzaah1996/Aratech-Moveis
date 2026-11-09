package com.aratech.moveis.portaria.controleacesso;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.almoxarifado.recebimento.controller.RecebimentoController;
import com.aratech.moveis.almoxarifado.recebimento.service.imp.RecebimentoServiceImp;
import com.aratech.moveis.carregamento.controller.CarregamentoController;
import com.aratech.moveis.portaria.controleacesso.controller.ControleAcessoController;
import com.aratech.moveis.portaria.controleacesso.controller.ControleAcessoExceptionHandler;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.enums.StatusAcesso;
import com.aratech.moveis.portaria.controleacesso.exception.ControleAcessoException;
import com.aratech.moveis.portaria.controleacesso.service.ControleAcessoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

class ControleAcessoControllerTest {
    final ControleAcessoService service = mock(ControleAcessoService.class);
    MockMvc mvc;
    final String portaria = "/aratech/portaria/chegadas",
            almox = "/aratech/almoxarifado/recebimentos";

    @BeforeEach
    void setup() {
        mvc =
                MockMvcBuilders.standaloneSetup(
                                new CarregamentoController(service),
                                new ControleAcessoController(service),
                                new RecebimentoController(new RecebimentoServiceImp(service)))
                        .setControllerAdvice(new ControleAcessoExceptionHandler())
                        .build();
    }

    @Test
    void carregamentoUsaSeuSetor() throws Exception {
        when(service.filaSetor("CARREGAMENTO", 0, 8))
                .thenReturn(Page.empty(org.springframework.data.domain.PageRequest.of(0, 8)));
        mvc.perform(get("/aratech/carregamento/chegadas")).andExpect(status().isOk());
        mvc.perform(post("/aratech/carregamento/chegadas/42/autorizar")).andExpect(status().isOk());
        verify(service).filaSetor("CARREGAMENTO", 0, 8);
        verify(service).autorizarSetor("CARREGAMENTO", 42L);
    }

    @Test
    void filaDosDoisModulosCompartilhaDadosEPaginacao() throws Exception {
        when(service.fila(1, 8))
                .thenReturn(Page.empty(org.springframework.data.domain.PageRequest.of(1, 8)));
        when(service.filaRecebimento(1, 8))
                .thenReturn(Page.empty(org.springframework.data.domain.PageRequest.of(1, 8)));
        mvc.perform(get(portaria).param("pagina", "1").param("tamanho", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mvc.perform(get(almox).param("pagina", "1").param("tamanho", "8"))
                .andExpect(status().isOk());
        verify(service).fila(1, 8);
        verify(service).filaRecebimento(1, 8);
    }

    @Test
    void eventosDoRecebimentoEPortariaUsamIdEmVezDeNotaFiscal() throws Exception {
        mvc.perform(post(almox + "/42/autorizar")).andExpect(status().isOk());
        verify(service).autorizarSetor("ALMOXARIFADO", 42L);
        mvc.perform(post(portaria + "/42/entrada")).andExpect(status().isOk());
        verify(service).confirmarEntrada(42L);
        mvc.perform(post(almox + "/42/finalizar")).andExpect(status().isOk());
        verify(service).finalizarSetor("ALMOXARIFADO", 42L);
    }

    @Test
    void portariaFinalizaPeloId() throws Exception {
        mvc.perform(post(portaria + "/42/finalizar")).andExpect(status().isOk());
        verify(service).finalizar(42L);
    }

    @Test
    void chegadaJsonRetorna201() throws Exception {
        mvc.perform(
                        post(portaria)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"notaFiscal\":\"123\",\"empresa\":\"Empresa\",\"chaveOperacao\":\"e1db31a9-ac2b-44c4-9188-88cece7f43a9\"}"))
                .andExpect(status().isCreated());
        verify(service).registrar(any(ChegadaDTO.class));
    }

    @Test
    void corpoAusenteEJsonMalformadoRetornam400() throws Exception {
        mvc.perform(post(portaria).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        mvc.perform(post(portaria).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void conflitoDeEtapaRetorna409() throws Exception {
        when(service.finalizarSetor("ALMOXARIFADO", 42L))
                .thenThrow(
                        new ControleAcessoException(
                                HttpStatus.CONFLICT, "Confirme a entrada primeiro."));
        mvc.perform(post(almox + "/42/finalizar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Confirme a entrada primeiro."));
    }

    @Test
    void erroInternoNaoExpoeDetalhes() throws Exception {
        when(service.resumoSetor("ALMOXARIFADO"))
                .thenThrow(new RuntimeException("Detalhes SQL internos"));
        mvc.perform(get(almox + "/resumo"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Nao foi possivel concluir a operacao."));
    }

    @Test
    void dataInvalidaNoHistoricoRetorna400() throws Exception {
        mvc.perform(get(portaria + "/historico").param("data", "2026-99-99"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void almoxarifadoEditaOMesmoRegistroDaPortaria() throws Exception {
        mvc.perform(
                        put(almox + "/42")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"notaFiscal\":\"123\",\"empresa\":\"Empresa\"}"))
                .andExpect(status().isOk());
        verify(service).editarSetor(eq("ALMOXARIFADO"), eq(42L), any(ChegadaDTO.class));
    }

    @Test
    void almoxarifadoNaoEditaCargaJaAutorizada() throws Exception {
        when(service.editarSetor(eq("ALMOXARIFADO"), eq(42L), any()))
                .thenThrow(
                        new ControleAcessoException(
                                HttpStatus.CONFLICT,
                                "Somente chegadas aguardando autorizacao podem ser editadas."));
        mvc.perform(
                        put(almox + "/42")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"notaFiscal\":\"123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void historicoDosDoisModulosContemAMesmaEntregaFinalizada() throws Exception {
        var instante = Instant.parse("2026-10-30T15:00:00Z");
        var entrega =
                new ChegadaResumoDTO(
                        42L,
                        "123",
                        "Empresa",
                        "Motorista",
                        "ABC1234",
                        "Madeira",
                        "ALMOXARIFADO",
                        StatusAcesso.FINALIZADO,
                        instante,
                        instante,
                        instante,
                        instante);
        when(service.historico(0, 8, null, null))
                .thenReturn(
                        new org.springframework.data.domain.PageImpl<>(
                                List.of(entrega),
                                org.springframework.data.domain.PageRequest.of(0, 8),
                                1));
        when(service.historicoRecebimento(0, 8))
                .thenReturn(
                        new org.springframework.data.domain.PageImpl<>(
                                List.of(entrega),
                                org.springframework.data.domain.PageRequest.of(0, 8),
                                1));
        mvc.perform(get(portaria + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(42))
                .andExpect(jsonPath("$.content[0].status").value("FINALIZADO"));
        mvc.perform(get(almox + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(42))
                .andExpect(jsonPath("$.content[0].dataFinalizacao").exists());
    }

    @Test
    void historicosVaziosAntesDaFinalizacao() throws Exception {
        when(service.historico(0, 8, null, null))
                .thenReturn(Page.empty(org.springframework.data.domain.PageRequest.of(0, 8)));
        when(service.historicoRecebimento(0, 8))
                .thenReturn(Page.empty(org.springframework.data.domain.PageRequest.of(0, 8)));
        mvc.perform(get(portaria + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(get(almox + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
