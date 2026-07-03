package com.aratechmoveis.almoxarifado.produto.controller;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;
import com.aratechmoveis.almoxarifado.produto.service.ProdutoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ProdutoController")
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProdutoService produtoService;

    private ProdutoDTO umProdutoDTOValido() {
        ProdutoDTO dto = new ProdutoDTO();
        dto.setCategoriaID(1L);
        dto.setFornecedorID(1L);
        dto.setNome("Cadeira de Escritório");
        dto.setSku("SKU-001");
        dto.setQuantidade(10);
        dto.setLocalArmazenamento("Prateleira A01");
        return dto;
    }

    @Nested
    @DisplayName("POST /almoxarifado/produto/add")
    class AddProduto {

        @Test
        @DisplayName("deve retornar 201 quando o produto é criado com sucesso")
        void deveRetornar201QuandoProdutoCriadoComSucesso() throws Exception {
            ProdutoDTO dto = umProdutoDTOValido();
            Response response = Response.builder().status(201).message("Produto criado com sucesso").produto(dto).build();

            given(produtoService.addProduto(any(ProdutoDTO.class))).willReturn(response);

            mockMvc.perform(post("/almoxarifado/produto/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("Produto criado com sucesso"));
        }

        @Test
        @DisplayName("deve retornar 400 quando o nome do produto não é informado")
        void deveRetornar400QuandoNomeNaoInformado() throws Exception {
            ProdutoDTO dto = umProdutoDTOValido();
            dto.setNome(null);

            mockMvc.perform(post("/almoxarifado/produto/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando o SKU contém caracteres inválidos")
        void deveRetornar400QuandoSkuInvalido() throws Exception {
            ProdutoDTO dto = umProdutoDTOValido();
            dto.setSku("SKU INVÁLIDO!");

            mockMvc.perform(post("/almoxarifado/produto/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 409 quando o serviço identifica SKU duplicado")
        void deveRetornar409QuandoSkuDuplicado() throws Exception {
            ProdutoDTO dto = umProdutoDTOValido();

            given(produtoService.addProduto(any(ProdutoDTO.class)))
                    .willThrow(new RecursoJaExistenteException("Já existe um produto cadastrado com o SKU: " + dto.getSku()));

            mockMvc.perform(post("/almoxarifado/produto/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("deve retornar 404 quando categoria ou fornecedor não são encontrados")
        void deveRetornar404QuandoCategoriaOuFornecedorNaoEncontrados() throws Exception {
            ProdutoDTO dto = umProdutoDTOValido();

            given(produtoService.addProduto(any(ProdutoDTO.class)))
                    .willThrow(new NotFoundException("Categoria não encontrada"));

            mockMvc.perform(post("/almoxarifado/produto/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /almoxarifado/produto/update/{id}")
    class UpdateProduto {

        @Test
        @DisplayName("deve retornar 200 quando o produto é atualizado com sucesso")
        void deveRetornar200QuandoProdutoAtualizadoComSucesso() throws Exception {
            ProdutoDTO dto = new ProdutoDTO();
            dto.setNome("Novo Nome");
            Response response = Response.builder().status(200).message("Produto atualizado com sucesso").produto(dto).build();

            given(produtoService.updateProduto(eq(1L), any(ProdutoDTO.class))).willReturn(response);

            mockMvc.perform(put("/almoxarifado/produto/update/1")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }

        @Test
        @DisplayName("deve retornar 400 quando o id do path é menor que 1")
        void deveRetornar400QuandoIdInvalido() throws Exception {
            mockMvc.perform(put("/almoxarifado/produto/update/0")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(new ProdutoDTO())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 404 quando o produto não é encontrado")
        void deveRetornar404QuandoProdutoNaoEncontrado() throws Exception {
            given(produtoService.updateProduto(eq(99L), any(ProdutoDTO.class)))
                    .willThrow(new NotFoundException("Produto não encontrado"));

            mockMvc.perform(put("/almoxarifado/produto/update/99")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(new ProdutoDTO())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /almoxarifado/produto/all")
    class GetProdutos {

        @Test
        @DisplayName("deve retornar 200 com a lista de produtos")
        void deveRetornar200ComListaDeProdutos() throws Exception {
            Response response = Response.builder().status(200).message("Produtos listados com sucesso")
                    .produtos(java.util.List.of(umProdutoDTOValido())).build();

            given(produtoService.getProdutos()).willReturn(response);

            mockMvc.perform(get("/almoxarifado/produto/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.produtos", org.hamcrest.Matchers.hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /almoxarifado/produto/{id}")
    class GetProdutoById {

        @Test
        @DisplayName("deve retornar 200 quando o produto existe")
        void deveRetornar200QuandoProdutoExiste() throws Exception {
            Response response = Response.builder().status(200).message("Produto listado com sucesso").produto(umProdutoDTOValido()).build();

            given(produtoService.getProdutoById(1L)).willReturn(response);

            mockMvc.perform(get("/almoxarifado/produto/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.produto.sku").value("SKU-001"));
        }

        @Test
        @DisplayName("deve retornar 404 quando o produto não existe")
        void deveRetornar404QuandoProdutoNaoExiste() throws Exception {
            given(produtoService.getProdutoById(99L))
                    .willThrow(new NotFoundException("Produto não encontrado, confira se o id está correto"));

            mockMvc.perform(get("/almoxarifado/produto/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 400 quando o id informado é inválido")
        void deveRetornar400QuandoIdInvalido() throws Exception {
            mockMvc.perform(get("/almoxarifado/produto/0"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /almoxarifado/produto/delete/{id}")
    class DeleteProduto {

        @Test
        @DisplayName("deve retornar 204 quando o produto é deletado com sucesso")
        void deveRetornar204QuandoProdutoDeletadoComSucesso() throws Exception {
            Response response = Response.builder().status(204).message("Produto deletado com sucesso").build();

            given(produtoService.deleteProduto(1L)).willReturn(response);

            mockMvc.perform(delete("/almoxarifado/produto/delete/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve retornar 404 quando o produto não existe")
        void deveRetornar404QuandoProdutoNaoExiste() throws Exception {
            given(produtoService.deleteProduto(99L))
                    .willThrow(new NotFoundException("Produto não encontrado, para deletar, confira se o id está correto"));

            mockMvc.perform(delete("/almoxarifado/produto/delete/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
