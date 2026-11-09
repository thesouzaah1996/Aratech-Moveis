package com.aratech.moveis.almoxarifado.fornecedores.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.almoxarifado.fornecedores.dto.FornecedorDTO;
import com.aratech.moveis.almoxarifado.fornecedores.dto.Response;
import com.aratech.moveis.almoxarifado.fornecedores.exception.FornecedorException;
import com.aratech.moveis.almoxarifado.fornecedores.service.FornecedorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class FornecedorControllerTest {
    private final FornecedorService service = mock(FornecedorService.class);
    private MockMvc mvc;
    private static final String BASE = "/aratech/almoxarifado/fornecedores";

    @BeforeEach
    void configurar() {
        mvc =
                MockMvcBuilders.standaloneSetup(new FornecedorController(service))
                        .setControllerAdvice(new FornecedorExceptionHandler())
                        .build();
    }

    @Test
    void cadastroJsonRetorna201EId() throws Exception {
        when(service.addFornecedor(any()))
                .thenReturn(
                        Response.builder()
                                .status(201)
                                .fornecedorId(42L)
                                .nome("Ferragens")
                                .ativo(true)
                                .build());
        mvc.perform(
                        post(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nome\":\"Ferragens\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fornecedorId").value(42))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void corpoAusenteOuJsonMalformadoRetorna400SemChamarService() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void retornaErrosPorCampoParaNomeEmBranco() throws Exception {
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var erros = factory.getValidator().validate(new FornecedorDTO());
            when(service.addFornecedor(any()))
                    .thenThrow(new jakarta.validation.ConstraintViolationException(erros));
        }
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists());
    }

    @Test
    void duplicaNomeRetorna409() throws Exception {
        when(service.addFornecedor(any()))
                .thenThrow(new FornecedorException(HttpStatus.CONFLICT, "Nome ja existe."));
        mvc.perform(
                        post(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nome\":\"Ferragens\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void paginaEConsultaPorIdUsamEndpointsGet() throws Exception {
        when(service.getAllFornecedores(1, 10))
                .thenReturn(
                        Response.builder()
                                .status(200)
                                .fornecedores(List.of())
                                .pagina(1)
                                .tamanho(10)
                                .total(0L)
                                .build());
        mvc.perform(get(BASE).param("pagina", "1").param("tamanho", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fornecedores").isArray());
        when(service.getFornecedorById(42L))
                .thenThrow(new FornecedorException(HttpStatus.NOT_FOUND, "Nao encontrada."));
        mvc.perform(get(BASE + "/42")).andExpect(status().isNotFound());
        mvc.perform(get(BASE + "/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void atualizaJsonEAlteraStatusComPatch() throws Exception {
        when(service.updateFornecedor(any(), eq(42L)))
                .thenReturn(Response.builder().status(200).nome("Madeiras").build());
        mvc.perform(
                        put(BASE + "/42")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nome\":\"Madeiras\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Madeiras"));
        when(service.enableFornecedor(42L))
                .thenReturn(Response.builder().status(200).ativo(true).build());
        when(service.disableFornecedor(42L))
                .thenReturn(Response.builder().status(200).ativo(false).build());
        mvc.perform(patch(BASE + "/42/ativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
        mvc.perform(patch(BASE + "/42/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void falhaInternaNaoExpoeDetalhesDoBanco() throws Exception {
        when(service.getFornecedorById(42L))
                .thenThrow(new RuntimeException("senha e SQL privados"));
        mvc.perform(get(BASE + "/42"))
                .andExpect(status().isInternalServerError())
                .andExpect(
                        jsonPath("$.mensagem")
                                .value("Nao foi possivel concluir a operacao. Tente novamente."));
    }
}
