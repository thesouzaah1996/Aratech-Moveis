package com.aratech.moveis.almoxarifado.produtos.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.almoxarifado.produtos.dto.Response;
import com.aratech.moveis.almoxarifado.produtos.exception.ProdutoException;
import com.aratech.moveis.almoxarifado.produtos.service.ProdutoService;
import com.aratech.moveis.almoxarifado.produtos.validation.ProdutoValidator;

import jakarta.validation.Validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProdutoControllerTest {
    private final ProdutoService service = mock(ProdutoService.class);
    private MockMvc mvc;
    private static final String BASE = "/aratech/almoxarifado/produtos";

    @BeforeEach
    void configurar() {
        mvc =
                MockMvcBuilders.standaloneSetup(new ProdutoController(service))
                        .setControllerAdvice(new ProdutoExceptionHandler())
                        .build();
    }

    @Test
    void cadastroRetorna201() throws Exception {
        when(service.addProduto(any()))
                .thenReturn(Response.builder().status(201).mensagem("Cadastrado").build());
        mvc.perform(
                        post(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nome\":\"Parafuso\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void edicaoNaoAceitaCampoQuantidadeNemCamposInternos() throws Exception {
        for (String campo : new String[] {"quantidade", "id", "ativo", "criadoEm"}) {
            mvc.perform(
                            put(BASE + "/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"" + campo + "\":1}"))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void corpoInvalidoEIdInvalidoRetornam400() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(BASE + "/abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void quantidadeFracionariaNaoETruncadaNoJson() throws Exception {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = new ProdutoValidator(factory.getValidator());
            when(service.saida(eq(1L), any()))
                    .thenAnswer(
                            i -> {
                                validator.validar(i.getArgument(1));
                                return null;
                            });
            mvc.perform(
                            post(BASE + "/1/saidas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"quantidade\":0.5,\"chaveOperacao\":\"00000000-0000-0000-0000-000000000001\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erros.quantidade").exists());
        }
    }

    @Test
    void movimentacaoUsaEndpointProprio() throws Exception {
        when(service.entrada(eq(1L), any())).thenReturn(Response.builder().status(200).build());
        mvc.perform(
                        post(BASE + "/1/entradas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"quantidade\":5,\"chaveOperacao\":\"00000000-0000-0000-0000-000000000001\"}"))
                .andExpect(status().isOk());
        verify(service).entrada(eq(1L), any());
        verify(service, never()).updateProduto(any(), any());
    }

    @Test
    void semEstoqueRetorna409() throws Exception {
        when(service.saida(eq(1L), any()))
                .thenThrow(new ProdutoException(HttpStatus.CONFLICT, "Sem estoque"));
        mvc.perform(
                        post(BASE + "/1/saidas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantidade\":5}"))
                .andExpect(status().isConflict());
    }
}
