//package com.aratechmoveis.almoxarifado.categoria.controller;
//
//import com.aratechmoveis.almoxarifado.res.Response;
//import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaDTO;
//import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaLookupDTO;
//import com.aratechmoveis.almoxarifado.categoria.service.CategoriaService;
//import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
//import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
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
//@WebMvcTest(CategoriaController.class)
//@DisplayName("CategoriaController")
//class CategoriaControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private CategoriaService categoriaService;
//
//    private CategoriaDTO umaCategoriaDTO() {
//        CategoriaDTO dto = new CategoriaDTO();
//        dto.setNome("Madeira");
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
//    @DisplayName("POST /almoxarifado/categoria/add")
//    class AddCategoria {
//
//        @Test
//        @DisplayName("deve retornar 201 quando categoria criada com sucesso")
//        void deveRetornar201QuandoCriadaComSucesso() throws Exception {
//            CategoriaDTO dto = umaCategoriaDTO();
//            given(categoriaService.criarCategoria(any())).willReturn(umaResponse(201, "Categoria criada com sucesso"));
//
//            mockMvc.perform(post("/almoxarifado/categoria/add")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isCreated())
//                    .andExpect(jsonPath("$.status").value(201))
//                    .andExpect(jsonPath("$.message").value("Categoria criada com sucesso"));
//        }
//
//        @Test
//        @DisplayName("deve retornar 400 quando corpo da requisição é inválido")
//        void deveRetornar400QuandoRequisicaoInvalida() throws Exception {
//            CategoriaDTO dto = new CategoriaDTO();
//
//            mockMvc.perform(post("/almoxarifado/categoria/add")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isBadRequest());
//        }
//
//        @Test
//        @DisplayName("deve retornar 409 quando nome já cadastrado")
//        void deveRetornar409QuandoNomeDuplicado() throws Exception {
//            CategoriaDTO dto = umaCategoriaDTO();
//            given(categoriaService.criarCategoria(any())).willThrow(RecursoJaExistenteException.class);
//
//            mockMvc.perform(post("/almoxarifado/categoria/add")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isConflict());
//        }
//    }
//
//    @Nested
//    @DisplayName("GET /almoxarifado/categoria/all")
//    class GetAllCategorias {
//
//        @Test
//        @DisplayName("deve retornar 200 com lista de categorias")
//        void deveRetornar200ComListaDeCategorias() throws Exception {
//            Response response = Response.builder()
//                    .status(200)
//                    .message("Categorias listadas com sucesso")
//                    .categorias(List.of(umaCategoriaDTO()))
//                    .build();
//
//            given(categoriaService.getCategorias()).willReturn(response);
//
//            mockMvc.perform(get("/almoxarifado/categoria/all"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.categorias").isArray())
//                    .andExpect(jsonPath("$.categorias[0].nome").value("Madeira"));
//        }
//    }
//
//    @Nested
//    @DisplayName("PUT /almoxarifado/categoria/update/{id}")
//    class UpdateCategoria {
//
//        @Test
//        @DisplayName("deve retornar 200 quando atualizada com sucesso")
//        void deveRetornar200QuandoAtualizadaComSucesso() throws Exception {
//            CategoriaDTO dto = umaCategoriaDTO();
//            given(categoriaService.updateCategoria(eq(1L), any())).willReturn(umaResponse(200, "Categoria alterada com sucesso"));
//
//            mockMvc.perform(put("/almoxarifado/categoria/update/1")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.message").value("Categoria alterada com sucesso"));
//        }
//
//        @Test
//        @DisplayName("deve retornar 404 quando categoria não encontrada")
//        void deveRetornar404QuandoNaoEncontrada() throws Exception {
//            given(categoriaService.updateCategoria(eq(99L), any())).willThrow(NotFoundException.class);
//
//            mockMvc.perform(put("/almoxarifado/categoria/update/99")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(umaCategoriaDTO())))
//                    .andExpect(status().isNotFound());
//        }
//    }
//
//    @Nested
//    @DisplayName("DELETE /almoxarifado/categoria/delete/{id}")
//    class DeleteCategoria {
//
//        @Test
//        @DisplayName("deve retornar 204 quando deletada com sucesso")
//        void deveRetornar204QuandoDeletadaComSucesso() throws Exception {
//            given(categoriaService.deleteCategoria(1L)).willReturn(umaResponse(204, "Categoria deletada com sucesso"));
//
//            mockMvc.perform(delete("/almoxarifado/categoria/delete/1"))
//                    .andExpect(status().isNoContent())
//                    .andExpect(jsonPath("$.message").value("Categoria deletada com sucesso"));
//        }
//
//        @Test
//        @DisplayName("deve retornar 404 quando categoria não encontrada")
//        void deveRetornar404QuandoNaoEncontrada() throws Exception {
//            given(categoriaService.deleteCategoria(99L)).willThrow(NotFoundException.class);
//
//            mockMvc.perform(delete("/almoxarifado/categoria/delete/99"))
//                    .andExpect(status().isNotFound());
//        }
//
//        @Test
//        @DisplayName("deve retornar 409 quando categoria possui produtos vinculados")
//        void deveRetornar409QuandoCategoriaPossuiProdutos() throws Exception {
//            given(categoriaService.deleteCategoria(1L)).willThrow(RecursoJaExistenteException.class);
//
//            mockMvc.perform(delete("/almoxarifado/categoria/delete/1"))
//                    .andExpect(status().isConflict());
//        }
//    }
//
//    @Nested
//    @DisplayName("GET /almoxarifado/categoria/lookup-categoria")
//    class LookupCategoria {
//
//        @Test
//        @DisplayName("deve retornar 200 com lista de lookup")
//        void deveRetornar200ComListaDeLookup() throws Exception {
//            Response response = Response.builder()
//                    .status(200)
//                    .message("Lookup de categorias carregado com sucesso")
//                    .categoriaLookup(List.of(new CategoriaLookupDTO(1L, "Madeira")))
//                    .build();
//
//            given(categoriaService.lookupCategoria()).willReturn(response);
//
//            mockMvc.perform(get("/almoxarifado/categoria/lookup-categoria"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.categoriaLookup").isArray())
//                    .andExpect(jsonPath("$.categoriaLookup[0].nome").value("Madeira"));
//        }
//    }
//}
