package com.aratechmoveis.manutencao.solicitacaopeca.controller;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.chamado.entity.Prioridade;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.solicitacaopeca.dto.SolicitacaoPecaDTO;
import com.aratechmoveis.manutencao.solicitacaopeca.entity.FinalidadePeca;
import com.aratechmoveis.manutencao.solicitacaopeca.service.SolicitacaoPecaService;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SolicitacaoPecaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SolicitacaoPecaController")
class SolicitacaoPecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SolicitacaoPecaService solicitacaoPecaService;

    private SolicitacaoPecaDTO umaSolicitacaoPecaDTOValida() {
        SolicitacaoPecaDTO dto = new SolicitacaoPecaDTO();
        dto.setNomePeca("Correia dentada");
        dto.setCodigo("COR-100");
        dto.setQuantidade(2);
        dto.setUnidade("un");
        dto.setEquipamento("Torno CNC 03");
        dto.setFinalidade(FinalidadePeca.CORRETIVA);
        dto.setPrioridade(Prioridade.ALTA);
        dto.setSolicitante("Carlos Ferreira");
        dto.setSetor("Produção");
        dto.setObservacoes("Necessário para reparo urgente");
        return dto;
    }

    @Nested
    @DisplayName("POST /manutencao/solicitacao-peca/add")
    class AddSolicitacaoPeca {

        @Test
        @DisplayName("deve retornar 201 quando a solicitação é criada com sucesso")
        void deveRetornar201QuandoSolicitacaoCriadaComSucesso() throws Exception {
            SolicitacaoPecaDTO dto = umaSolicitacaoPecaDTOValida();
            Response response = Response.builder().status(201).message("Solicitação de peça criada com sucesso").solicitacaoPeca(dto).build();

            given(solicitacaoPecaService.addSolicitacaoPeca(any(SolicitacaoPecaDTO.class))).willReturn(response);

            mockMvc.perform(post("/manutencao/solicitacao-peca/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201));
        }

        @Test
        @DisplayName("deve retornar 400 quando as observações não são informadas")
        void deveRetornar400QuandoObservacoesNaoInformadas() throws Exception {
            SolicitacaoPecaDTO dto = umaSolicitacaoPecaDTOValida();
            dto.setObservacoes(null);

            mockMvc.perform(post("/manutencao/solicitacao-peca/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando a quantidade é menor que 1")
        void deveRetornar400QuandoQuantidadeInvalida() throws Exception {
            SolicitacaoPecaDTO dto = umaSolicitacaoPecaDTOValida();
            dto.setQuantidade(0);

            mockMvc.perform(post("/manutencao/solicitacao-peca/add")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /manutencao/solicitacao-peca/all")
    class GetSolicitacoesPeca {

        @Test
        @DisplayName("deve retornar 200 com a lista de solicitações")
        void deveRetornar200ComListaDeSolicitacoes() throws Exception {
            Response response = Response.builder().status(200).message("Solicitações de peça listadas com sucesso")
                    .solicitacoesPeca(List.of(umaSolicitacaoPecaDTOValida())).build();

            given(solicitacaoPecaService.getSolicitacoesPeca()).willReturn(response);

            mockMvc.perform(get("/manutencao/solicitacao-peca/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.solicitacoesPeca", org.hamcrest.Matchers.hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /manutencao/solicitacao-peca/{id}")
    class GetSolicitacaoPecaById {

        @Test
        @DisplayName("deve retornar 200 quando a solicitação existe")
        void deveRetornar200QuandoSolicitacaoExiste() throws Exception {
            Response response = Response.builder().status(200).message("Solicitação de peça listada com sucesso")
                    .solicitacaoPeca(umaSolicitacaoPecaDTOValida()).build();

            given(solicitacaoPecaService.getSolicitacaoPecaById(1L)).willReturn(response);

            mockMvc.perform(get("/manutencao/solicitacao-peca/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.solicitacaoPeca.nomePeca").value("Correia dentada"));
        }

        @Test
        @DisplayName("deve retornar 404 quando a solicitação não existe")
        void deveRetornar404QuandoSolicitacaoNaoExiste() throws Exception {
            given(solicitacaoPecaService.getSolicitacaoPecaById(99L))
                    .willThrow(new NotFoundException("Solicitação de peça não encontrada, confira se o id está correto"));

            mockMvc.perform(get("/manutencao/solicitacao-peca/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 400 quando o id informado é inválido")
        void deveRetornar400QuandoIdInvalido() throws Exception {
            mockMvc.perform(get("/manutencao/solicitacao-peca/0"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /manutencao/solicitacao-peca/delete/{id}")
    class DeleteSolicitacaoPeca {

        @Test
        @DisplayName("deve retornar 204 quando a solicitação é deletada com sucesso")
        void deveRetornar204QuandoSolicitacaoDeletadaComSucesso() throws Exception {
            Response response = Response.builder().status(204).message("Solicitação de peça deletada com sucesso").build();

            given(solicitacaoPecaService.deleteSolicitacaoPeca(1L)).willReturn(response);

            mockMvc.perform(delete("/manutencao/solicitacao-peca/delete/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve retornar 404 quando a solicitação não existe")
        void deveRetornar404QuandoSolicitacaoNaoExiste() throws Exception {
            given(solicitacaoPecaService.deleteSolicitacaoPeca(99L))
                    .willThrow(new NotFoundException("Solicitação de peça não encontrada, para deletar, confira se o id está correto"));

            mockMvc.perform(delete("/manutencao/solicitacao-peca/delete/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
