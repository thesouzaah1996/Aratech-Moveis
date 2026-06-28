//package com.aratechmoveis.almoxarifado.fornecedor.controller;
//
//import com.aratechmoveis.almoxarifado.response.Response;
//import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
//import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
//import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorDTO;
//import com.aratechmoveis.almoxarifado.fornecedor.service.FornecedorService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.willThrow;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(FornecedorController.class)
//@DisplayName("FornecedorController")
//class FornecedorControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private FornecedorService fornecedorService;
//
//    private FornecedorDTO umFornecedorDTO() {
//        FornecedorDTO dto = new FornecedorDTO();
//        dto.setNome("Fornecedor Teste");
//        dto.setEmail("teste@email.com");
//        dto.setTelefone("(11) 1234-5678");
//        return dto;
//    }
//
//    private Response umaResponse(int status, String message) {
//        return Response.builder()
//                .status(status)
//                .message(message)
//                .build();
//    }
//
//    @Nested
//    @DisplayName("POST /almoxarifado/fornecedor/add")
//    class AddFornecedor {
//
//        @Test
//        @DisplayName("deve retornar 201 quando fornecedor criado com sucesso")
//        void deveRetornar201QuandoCriadoComSucesso() throws Exception {
//            FornecedorDTO dto = umFornecedorDTO();
//            given(fornecedorService.addFornecedor(any())).willReturn(umaResponse(201, "Fornecedor adicionado com sucesso"));
//
//            mockMvc.perform(post("/almoxarifado/fornecedor/add")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isCreated())
//                    .andExpect(jsonPath("$.status").value(201))
//                    .andExpect(jsonPath("$.message").value("Fornecedor adicionado com sucesso"));
//        }
//
//        @Test
//        @DisplayName("deve retornar 400 quando corpo da requisição é inválido")
//        void deveRetornar400QuandoRequisicaoInvalida() throws Exception {
//            FornecedorDTO dto = new FornecedorDTO();
//
//            mockMvc.perform(post("/almoxarifado/fornecedor/add")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isBadRequest());
//        }
//
//        @Test
//        @DisplayName("deve retornar 409 quando e-mail já cadastrado")
//        void deveRetornar409QuandoEmailDuplicado() throws Exception {
//            FornecedorDTO dto = umFornecedorDTO();
//            given(fornecedorService.addFornecedor(any())).willThrow(RecursoJaExistenteException.class);
//
//            mockMvc.perform(post("/almoxarifado/fornecedor/add")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isConflict());
//        }
//    }
//
//    @Nested
//    @DisplayName("GET /almoxarifado/fornecedor/all")
//    class GetAllFornecedores {
//
//        @Test
//        @DisplayName("deve retornar 200 com lista de fornecedores")
//        void deveRetornar200ComListaDeFornecedores() throws Exception {
//            Response response = Response.builder()
//                    .status(200)
//                    .message("Fornecedores listados com sucesso")
//                    .fornecedores(List.of(umFornecedorDTO()))
//                    .build();
//
//            given(fornecedorService.getFornecedores()).willReturn(response);
//
//            mockMvc.perform(get("/almoxarifado/fornecedor/all"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.fornecedores").isArray())
//                    .andExpect(jsonPath("$.fornecedores[0].nome").value("Fornecedor Teste"));
//        }
//    }
//
//    @Nested
//    @DisplayName("PUT /almoxarifado/fornecedor/update/{id}")
//    class UpdateFornecedor {
//
//        @Test
//        @DisplayName("deve retornar 200 quando atualizado com sucesso")
//        void deveRetornar200QuandoAtualizadoComSucesso() throws Exception {
//            FornecedorDTO dto = umFornecedorDTO();
//            given(fornecedorService.updateFornecedor(eq(1L), any())).willReturn(umaResponse(200, "Fornecedor atualizado com sucesso"));
//
//            mockMvc.perform(put("/almoxarifado/fornecedor/update/1")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.message").value("Fornecedor atualizado com sucesso"));
//        }
//
//        @Test
//        @DisplayName("deve retornar 404 quando fornecedor não encontrado")
//        void deveRetornar404QuandoNaoEncontrado() throws Exception {
//            given(fornecedorService.updateFornecedor(eq(99L), any())).willThrow(NotFoundException.class);
//
//            mockMvc.perform(put("/almoxarifado/fornecedor/update/99")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(umFornecedorDTO())))
//                    .andExpect(status().isNotFound());
//        }
//    }
//
//    @Nested
//    @DisplayName("POST /almoxarifado/fornecedor/disable/{id}")
//    class DisableFornecedor {
//
//        @Test
//        @DisplayName("deve retornar 200 quando desativado com sucesso")
//        void deveRetornar200QuandoDesativadoComSucesso() throws Exception {
//            given(fornecedorService.disableFornecedor(1L)).willReturn(umaResponse(200, "Fornecedor desabilitado com sucesso"));
//
//            mockMvc.perform(post("/almoxarifado/fornecedor/disable/1"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.message").value("Fornecedor desabilitado com sucesso"));
//        }
//
//        @Test
//        @DisplayName("deve retornar 404 quando fornecedor não encontrado")
//        void deveRetornar404QuandoNaoEncontrado() throws Exception {
//            given(fornecedorService.disableFornecedor(99L)).willThrow(NotFoundException.class);
//
//            mockMvc.perform(post("/almoxarifado/fornecedor/disable/99"))
//                    .andExpect(status().isNotFound());
//        }
//    }
//
//    @Nested
//    @DisplayName("POST /almoxarifado/fornecedor/enable/{id}")
//    class EnableFornecedor {
//
//        @Test
//        @DisplayName("deve retornar 200 quando reativado com sucesso")
//        void deveRetornar200QuandoReativadoComSucesso() throws Exception {
//            given(fornecedorService.enableFornecedor(1L)).willReturn(umaResponse(200, "Fornecedor ativado com sucesso."));
//
//            mockMvc.perform(post("/almoxarifado/fornecedor/enable/1"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.message").value("Fornecedor ativado com sucesso."));
//        }
//
//        @Test
//        @DisplayName("deve retornar 404 quando fornecedor não encontrado")
//        void deveRetornar404QuandoNaoEncontrado() throws Exception {
//            given(fornecedorService.enableFornecedor(99L)).willThrow(NotFoundException.class);
//
//            mockMvc.perform(post("/almoxarifado/fornecedor/enable/99"))
//                    .andExpect(status().isNotFound());
//        }
//    }
//}
