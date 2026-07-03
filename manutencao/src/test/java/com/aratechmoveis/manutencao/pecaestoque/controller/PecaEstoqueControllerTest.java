package com.aratechmoveis.manutencao.pecaestoque.controller;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.manutencao.pecaestoque.dto.PecaEstoqueDTO;
import com.aratechmoveis.manutencao.pecaestoque.service.PecaEstoqueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PecaEstoqueController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PecaEstoqueController")
class PecaEstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PecaEstoqueService pecaEstoqueService;

    private PecaEstoqueDTO umaPecaEstoqueDTOValida() {
        PecaEstoqueDTO dto = new PecaEstoqueDTO();
        dto.setNome("Rolamento 6204");
        dto.setCodigo("ROL-6204");
        dto.setQuantidade(15);
        dto.setUnidade("un");
        dto.setLocalizacao("Prateleira A3");
        return dto;
    }

    @Nested
    @DisplayName("POST /manutencao/peca-estoque/add")
    class AddPecaEstoque {

        @Test
        @DisplayName("deve retornar 201 quando a peça é criada com sucesso")
        void deveRetornar201QuandoPecaCriadaComSucesso() throws Exception {
            PecaEstoqueDTO dto = umaPecaEstoqueDTOValida();
            Response response = Response.builder().status(201).message("Peça cadastrada com sucesso").pecaEstoque(dto).build();

            given(pecaEstoqueService.addPecaEstoque(any(PecaEstoqueDTO.class))).willReturn(response);

            mockMvc.perform(post("/manutencao/peca-estoque/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201));
        }

        @Test
        @DisplayName("deve retornar 400 quando o nome não é informado")
        void deveRetornar400QuandoNomeNaoInformado() throws Exception {
            PecaEstoqueDTO dto = umaPecaEstoqueDTOValida();
            dto.setNome(null);

            mockMvc.perform(post("/manutencao/peca-estoque/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 409 quando o serviço identifica código duplicado")
        void deveRetornar409QuandoCodigoDuplicado() throws Exception {
            PecaEstoqueDTO dto = umaPecaEstoqueDTOValida();

            given(pecaEstoqueService.addPecaEstoque(any(PecaEstoqueDTO.class)))
                    .willThrow(new RecursoJaExistenteException("Já existe uma peça cadastrada com o código: " + dto.getCodigo()));

            mockMvc.perform(post("/manutencao/peca-estoque/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PUT /manutencao/peca-estoque/update/{id}")
    class UpdatePecaEstoque {

        @Test
        @DisplayName("deve retornar 200 quando a peça é atualizada com sucesso")
        void deveRetornar200QuandoPecaAtualizadaComSucesso() throws Exception {
            PecaEstoqueDTO dto = new PecaEstoqueDTO();
            dto.setNome("Novo Nome");
            Response response = Response.builder().status(200).message("Peça atualizada com sucesso").pecaEstoque(dto).build();

            given(pecaEstoqueService.updatePecaEstoque(eq(1L), any(PecaEstoqueDTO.class))).willReturn(response);

            mockMvc.perform(put("/manutencao/peca-estoque/update/1")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 400 quando o id do path é menor que 1")
        void deveRetornar400QuandoIdInvalido() throws Exception {
            mockMvc.perform(put("/manutencao/peca-estoque/update/0")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(new PecaEstoqueDTO())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 404 quando a peça não é encontrada")
        void deveRetornar404QuandoPecaNaoEncontrada() throws Exception {
            given(pecaEstoqueService.updatePecaEstoque(eq(99L), any(PecaEstoqueDTO.class)))
                    .willThrow(new NotFoundException("Peça não encontrada"));

            mockMvc.perform(put("/manutencao/peca-estoque/update/99")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(new PecaEstoqueDTO())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /manutencao/peca-estoque/all")
    class GetPecasEstoque {

        @Test
        @DisplayName("deve retornar 200 com a lista de peças")
        void deveRetornar200ComListaDePecas() throws Exception {
            Response response = Response.builder().status(200).message("Peças listadas com sucesso")
                    .pecasEstoque(List.of(umaPecaEstoqueDTOValida())).build();

            given(pecaEstoqueService.getPecasEstoque()).willReturn(response);

            mockMvc.perform(get("/manutencao/peca-estoque/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pecasEstoque", org.hamcrest.Matchers.hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /manutencao/peca-estoque/{id}")
    class GetPecaEstoqueById {

        @Test
        @DisplayName("deve retornar 200 quando a peça existe")
        void deveRetornar200QuandoPecaExiste() throws Exception {
            Response response = Response.builder().status(200).message("Peça listada com sucesso").pecaEstoque(umaPecaEstoqueDTOValida()).build();

            given(pecaEstoqueService.getPecaEstoqueById(1L)).willReturn(response);

            mockMvc.perform(get("/manutencao/peca-estoque/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pecaEstoque.codigo").value("ROL-6204"));
        }

        @Test
        @DisplayName("deve retornar 404 quando a peça não existe")
        void deveRetornar404QuandoPecaNaoExiste() throws Exception {
            given(pecaEstoqueService.getPecaEstoqueById(99L))
                    .willThrow(new NotFoundException("Peça não encontrada, confira se o id está correto"));

            mockMvc.perform(get("/manutencao/peca-estoque/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 400 quando o id informado é inválido")
        void deveRetornar400QuandoIdInvalido() throws Exception {
            mockMvc.perform(get("/manutencao/peca-estoque/0"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /manutencao/peca-estoque/delete/{id}")
    class DeletePecaEstoque {

        @Test
        @DisplayName("deve retornar 204 quando a peça é deletada com sucesso")
        void deveRetornar204QuandoPecaDeletadaComSucesso() throws Exception {
            Response response = Response.builder().status(204).message("Peça deletada com sucesso").build();

            given(pecaEstoqueService.deletePecaEstoque(1L)).willReturn(response);

            mockMvc.perform(delete("/manutencao/peca-estoque/delete/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve retornar 404 quando a peça não existe")
        void deveRetornar404QuandoPecaNaoExiste() throws Exception {
            given(pecaEstoqueService.deletePecaEstoque(99L))
                    .willThrow(new NotFoundException("Peça não encontrada, para deletar, confira se o id está correto"));

            mockMvc.perform(delete("/manutencao/peca-estoque/delete/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
