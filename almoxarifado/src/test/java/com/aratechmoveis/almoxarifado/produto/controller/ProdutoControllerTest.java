package com.aratechmoveis.almoxarifado.produto.controller;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.exceptions.EstoqueInsuficienteException;
import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.almoxarifado.produto.dto.EntradaEstoqueDTO;
import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;
import com.aratechmoveis.almoxarifado.produto.dto.SaidaEstoqueDTO;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @DisplayName("POST /almoxarifado/produto/adicionar")
    class AdicionarProduto {

        @Test
        @DisplayName("deve retornar 201 quando o produto é criado com sucesso")
        void deveRetornar201QuandoProdutoCriadoComSucesso() throws Exception {
            ProdutoDTO dto = umProdutoDTOValido();
            Response response = Response.builder().status(201).mensagem("Produto criado com sucesso").produto(dto).build();

            given(produtoService.adicionarProduto(any(ProdutoDTO.class))).willReturn(response);

            mockMvc.perform(post("/almoxarifado/produto/adicionar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.mensagem").value("Produto criado com sucesso"));
        }

        @Test
        @DisplayName("deve retornar 400 quando o nome do produto não é informado")
        void deveRetornar400QuandoNomeNaoInformado() throws Exception {
            ProdutoDTO dto = umProdutoDTOValido();
            dto.setNome(null);

            mockMvc.perform(post("/almoxarifado/produto/adicionar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando o SKU contém caracteres inválidos")
        void deveRetornar400QuandoSkuInvalido() throws Exception {
            ProdutoDTO dto = umProdutoDTOValido();
            dto.setSku("SKU INVÁLIDO!");

            mockMvc.perform(post("/almoxarifado/produto/adicionar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve propagar RecursoJaExistenteException quando o serviço identifica SKU duplicado")
        void devePropagarExcecaoQuandoSkuDuplicado() {
            ProdutoDTO dto = umProdutoDTOValido();

            given(produtoService.adicionarProduto(any(ProdutoDTO.class)))
                    .willThrow(new RecursoJaExistenteException("Já existe um produto cadastrado com o SKU: " + dto.getSku()));

            assertThatThrownBy(() -> mockMvc.perform(post("/almoxarifado/produto/adicionar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto))))
                    .isInstanceOf(RecursoJaExistenteException.class);
        }

        @Test
        @DisplayName("deve propagar NotFoundException quando categoria ou fornecedor não são encontrados")
        void devePropagarExcecaoQuandoCategoriaOuFornecedorNaoEncontrados() {
            ProdutoDTO dto = umProdutoDTOValido();

            given(produtoService.adicionarProduto(any(ProdutoDTO.class)))
                    .willThrow(new NotFoundException("Categoria não encontrada"));

            assertThatThrownBy(() -> mockMvc.perform(post("/almoxarifado/produto/adicionar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto))))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("PUT /almoxarifado/produto/atualizar/{id}")
    class AtualizarProduto {

        @Test
        @DisplayName("deve retornar 200 quando o produto é atualizado com sucesso")
        void deveRetornar200QuandoProdutoAtualizadoComSucesso() throws Exception {
            ProdutoDTO dto = new ProdutoDTO();
            dto.setNome("Novo Nome");
            Response response = Response.builder().status(200).mensagem("Produto atualizado com sucesso").produto(dto).build();

            given(produtoService.atualizarProduto(eq(1L), any(ProdutoDTO.class))).willReturn(response);

            mockMvc.perform(put("/almoxarifado/produto/atualizar/1")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }

        @Test
        @DisplayName("deve retornar 400 quando o id do path é menor que 1")
        void deveRetornar400QuandoIdInvalido() throws Exception {
            mockMvc.perform(put("/almoxarifado/produto/atualizar/0")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(new ProdutoDTO())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve propagar NotFoundException quando o produto não é encontrado")
        void devePropagarExcecaoQuandoProdutoNaoEncontrado() {
            given(produtoService.atualizarProduto(eq(99L), any(ProdutoDTO.class)))
                    .willThrow(new NotFoundException("Produto não encontrado"));

            assertThatThrownBy(() -> mockMvc.perform(put("/almoxarifado/produto/atualizar/99")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(new ProdutoDTO()))))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("GET /almoxarifado/produto/todos")
    class ListarProdutos {

        @Test
        @DisplayName("deve retornar 200 com a lista de produtos")
        void deveRetornar200ComListaDeProdutos() throws Exception {
            Response response = Response.builder().status(200).mensagem("Produtos listados com sucesso")
                    .produtos(java.util.List.of(umProdutoDTOValido())).build();

            given(produtoService.listarProdutos()).willReturn(response);

            mockMvc.perform(get("/almoxarifado/produto/todos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.produtos", org.hamcrest.Matchers.hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /almoxarifado/produto/{id}")
    class BuscarProdutoPorId {

        @Test
        @DisplayName("deve retornar 200 quando o produto existe")
        void deveRetornar200QuandoProdutoExiste() throws Exception {
            Response response = Response.builder().status(200).mensagem("Produto listado com sucesso").produto(umProdutoDTOValido()).build();

            given(produtoService.buscarProdutoPorId(1L)).willReturn(response);

            mockMvc.perform(get("/almoxarifado/produto/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.produto.sku").value("SKU-001"));
        }

        @Test
        @DisplayName("deve propagar NotFoundException quando o produto não existe")
        void devePropagarExcecaoQuandoProdutoNaoExiste() {
            given(produtoService.buscarProdutoPorId(99L))
                    .willThrow(new NotFoundException("Produto não encontrado, confira se o id está correto"));

            assertThatThrownBy(() -> mockMvc.perform(get("/almoxarifado/produto/99")))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("deve retornar 400 quando o id informado é inválido")
        void deveRetornar400QuandoIdInvalido() throws Exception {
            mockMvc.perform(get("/almoxarifado/produto/0"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /almoxarifado/produto/remover/{id}")
    class RemoverProduto {

        @Test
        @DisplayName("deve retornar 204 quando o produto é deletado com sucesso")
        void deveRetornar204QuandoProdutoDeletadoComSucesso() throws Exception {
            Response response = Response.builder().status(204).mensagem("Produto deletado com sucesso").build();

            given(produtoService.removerProduto(1L)).willReturn(response);

            mockMvc.perform(delete("/almoxarifado/produto/remover/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve propagar NotFoundException quando o produto não existe")
        void devePropagarExcecaoQuandoProdutoNaoExiste() {
            given(produtoService.removerProduto(99L))
                    .willThrow(new NotFoundException("Produto não encontrado, para deletar, confira se o id está correto"));

            assertThatThrownBy(() -> mockMvc.perform(delete("/almoxarifado/produto/remover/99")))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("PUT /almoxarifado/produto/entrada-estoque/{sku}")
    class EntradaEstoque {

        @Test
        @DisplayName("deve retornar 200 quando a entrada de estoque é realizada com sucesso")
        void deveRetornar200QuandoEntradaRealizadaComSucesso() throws Exception {
            EntradaEstoqueDTO dto = new EntradaEstoqueDTO(5);
            Response response = Response.builder().status(200).mensagem("Entrada de estoque realizada com sucesso").produto(umProdutoDTOValido()).build();

            given(produtoService.entradaEstoque(eq("SKU-001"), any(EntradaEstoqueDTO.class))).willReturn(response);

            mockMvc.perform(put("/almoxarifado/produto/entrada-estoque/SKU-001")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Entrada de estoque realizada com sucesso"));
        }

        @Test
        @DisplayName("deve retornar 400 quando a quantidade informada é inválida")
        void deveRetornar400QuandoQuantidadeInvalida() throws Exception {
            EntradaEstoqueDTO dto = new EntradaEstoqueDTO(0);

            mockMvc.perform(put("/almoxarifado/produto/entrada-estoque/SKU-001")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve propagar NotFoundException quando o produto não é encontrado")
        void devePropagarExcecaoQuandoProdutoNaoEncontrado() {
            EntradaEstoqueDTO dto = new EntradaEstoqueDTO(5);

            given(produtoService.entradaEstoque(eq("SKU-INEXISTENTE"), any(EntradaEstoqueDTO.class)))
                    .willThrow(new NotFoundException("Produto não encontrado"));

            assertThatThrownBy(() -> mockMvc.perform(put("/almoxarifado/produto/entrada-estoque/SKU-INEXISTENTE")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto))))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("PUT /almoxarifado/produto/saida-estoque/{sku}")
    class SaidaEstoque {

        @Test
        @DisplayName("deve retornar 200 quando a saída de estoque é realizada com sucesso")
        void deveRetornar200QuandoSaidaRealizadaComSucesso() throws Exception {
            SaidaEstoqueDTO dto = new SaidaEstoqueDTO(5);
            Response response = Response.builder().status(200).mensagem("Saída de estoque realizada com sucesso").produto(umProdutoDTOValido()).build();

            given(produtoService.saidaEstoque(eq("SKU-001"), any(SaidaEstoqueDTO.class))).willReturn(response);

            mockMvc.perform(put("/almoxarifado/produto/saida-estoque/SKU-001")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem").value("Saída de estoque realizada com sucesso"));
        }

        @Test
        @DisplayName("deve retornar 400 quando a quantidade informada é inválida")
        void deveRetornar400QuandoQuantidadeInvalida() throws Exception {
            SaidaEstoqueDTO dto = new SaidaEstoqueDTO(-1);

            mockMvc.perform(put("/almoxarifado/produto/saida-estoque/SKU-001")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve propagar EstoqueInsuficienteException quando não há estoque suficiente")
        void devePropagarExcecaoQuandoEstoqueInsuficiente() {
            SaidaEstoqueDTO dto = new SaidaEstoqueDTO(50);

            given(produtoService.saidaEstoque(eq("SKU-001"), any(SaidaEstoqueDTO.class)))
                    .willThrow(new EstoqueInsuficienteException("Estoque insuficiente para: SKU-001"));

            assertThatThrownBy(() -> mockMvc.perform(put("/almoxarifado/produto/saida-estoque/SKU-001")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto))))
                    .isInstanceOf(EstoqueInsuficienteException.class);
        }

        @Test
        @DisplayName("deve propagar NotFoundException quando o produto não é encontrado")
        void devePropagarExcecaoQuandoProdutoNaoEncontrado() {
            SaidaEstoqueDTO dto = new SaidaEstoqueDTO(1);

            given(produtoService.saidaEstoque(eq("SKU-INEXISTENTE"), any(SaidaEstoqueDTO.class)))
                    .willThrow(new NotFoundException("Produto não encontrado"));

            assertThatThrownBy(() -> mockMvc.perform(put("/almoxarifado/produto/saida-estoque/SKU-INEXISTENTE")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto))))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
