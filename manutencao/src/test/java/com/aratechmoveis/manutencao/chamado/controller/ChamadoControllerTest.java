package com.aratechmoveis.manutencao.chamado.controller;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.chamado.dto.AtribuirMecanicoDTO;
import com.aratechmoveis.manutencao.chamado.dto.ChamadoDTO;
import com.aratechmoveis.manutencao.chamado.entity.Prioridade;
import com.aratechmoveis.manutencao.chamado.entity.StatusChamado;
import com.aratechmoveis.manutencao.chamado.entity.TipoManutencao;
import com.aratechmoveis.manutencao.chamado.service.ChamadoService;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.exceptions.RecursoJaExistenteException;
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

@WebMvcTest(ChamadoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChamadoController")
class ChamadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChamadoService chamadoService;

    private ChamadoDTO umChamadoDTOValido() {
        ChamadoDTO dto = new ChamadoDTO();
        dto.setEquipamento("Torno CNC 03");
        dto.setTipo(TipoManutencao.CORRETIVA);
        dto.setPrioridade(Prioridade.ALTA);
        dto.setSolicitante("Carlos Ferreira");
        dto.setSetor("Produção");
        dto.setDescricao("Equipamento com falha no motor");
        return dto;
    }

    @Nested
    @DisplayName("POST /manutencao/chamado/adicionar")
    class AddChamado {

        @Test
        @DisplayName("deve retornar 201 quando o chamado é criado com sucesso")
        void deveRetornar201QuandoChamadoCriadoComSucesso() throws Exception {
            ChamadoDTO dto = umChamadoDTOValido();
            Response response = Response.builder().status(201).message("Chamado de manutenção criado com sucesso").chamado(dto).build();

            given(chamadoService.adicionarChamado(any(ChamadoDTO.class))).willReturn(response);

            mockMvc.perform(post("/manutencao/chamado/adicionar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201));
        }

        @Test
        @DisplayName("deve retornar 400 quando o equipamento não é informado")
        void deveRetornar400QuandoEquipamentoNaoInformado() throws Exception {
            ChamadoDTO dto = umChamadoDTOValido();
            dto.setEquipamento(null);

            mockMvc.perform(post("/manutencao/chamado/adicionar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando o telefone informado é inválido")
        void deveRetornar400QuandoTelefoneInvalido() throws Exception {
            ChamadoDTO dto = umChamadoDTOValido();
            dto.setTelefone("11999998888");

            mockMvc.perform(post("/manutencao/chamado/adicionar")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /manutencao/chamado/todos")
    class GetChamados {

        @Test
        @DisplayName("deve retornar 200 com a lista de chamados")
        void deveRetornar200ComListaDeChamados() throws Exception {
            Response response = Response.builder().status(200).message("Chamados listados com sucesso")
                    .chamados(List.of(umChamadoDTOValido())).build();

            given(chamadoService.listarChamados(null)).willReturn(response);

            mockMvc.perform(get("/manutencao/chamado/todos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.chamados", org.hamcrest.Matchers.hasSize(1)));
        }

        @Test
        @DisplayName("deve repassar o status informado como filtro")
        void deveRepassarStatusComoFiltro() throws Exception {
            Response response = Response.builder().status(200).message("Chamados listados com sucesso")
                    .chamados(List.of()).build();

            given(chamadoService.listarChamados(StatusChamado.EM_MANUTENCAO)).willReturn(response);

            mockMvc.perform(get("/manutencao/chamado/todos").param("status", "EM_MANUTENCAO"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /manutencao/chamado/{id}")
    class GetChamadoById {

        @Test
        @DisplayName("deve retornar 200 quando o chamado existe")
        void deveRetornar200QuandoChamadoExiste() throws Exception {
            Response response = Response.builder().status(200).message("Chamado listado com sucesso").chamado(umChamadoDTOValido()).build();

            given(chamadoService.buscarChamadoPorId(1L)).willReturn(response);

            mockMvc.perform(get("/manutencao/chamado/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.chamado.equipamento").value("Torno CNC 03"));
        }

        @Test
        @DisplayName("deve retornar 404 quando o chamado não existe")
        void deveRetornar404QuandoChamadoNaoExiste() throws Exception {
            given(chamadoService.buscarChamadoPorId(99L))
                    .willThrow(new NotFoundException("Chamado não encontrado, confira se o id está correto"));

            mockMvc.perform(get("/manutencao/chamado/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deve retornar 400 quando o id informado é inválido")
        void deveRetornar400QuandoIdInvalido() throws Exception {
            mockMvc.perform(get("/manutencao/chamado/0"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /manutencao/chamado/atribuir/{id}")
    class AtribuirMecanico {

        @Test
        @DisplayName("deve retornar 200 quando o mecânico é atribuído com sucesso")
        void deveRetornar200QuandoAtribuidoComSucesso() throws Exception {
            AtribuirMecanicoDTO dto = new AtribuirMecanicoDTO("Joao Pedro");
            Response response = Response.builder().status(200).message("Chamado atribuído com sucesso").chamado(umChamadoDTOValido()).build();

            given(chamadoService.atribuirMecanico(eq(1L), any(AtribuirMecanicoDTO.class))).willReturn(response);

            mockMvc.perform(put("/manutencao/chamado/atribuir/1")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 400 quando o mecânico não é informado")
        void deveRetornar400QuandoMecanicoNaoInformado() throws Exception {
            mockMvc.perform(put("/manutencao/chamado/atribuir/1")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(new AtribuirMecanicoDTO())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 409 quando o chamado já está concluído")
        void deveRetornar409QuandoJaConcluido() throws Exception {
            given(chamadoService.atribuirMecanico(eq(1L), any(AtribuirMecanicoDTO.class)))
                    .willThrow(new RecursoJaExistenteException("Não é possível atribuir um mecânico a um chamado já concluído"));

            mockMvc.perform(put("/manutencao/chamado/atribuir/1")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(new AtribuirMecanicoDTO("Joao"))))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PUT /manutencao/chamado/concluir/{id}")
    class ConcluirChamado {

        @Test
        @DisplayName("deve retornar 200 quando o chamado é concluído com sucesso")
        void deveRetornar200QuandoConcluidoComSucesso() throws Exception {
            Response response = Response.builder().status(200).message("Chamado concluído com sucesso").chamado(umChamadoDTOValido()).build();

            given(chamadoService.concluirChamado(1L)).willReturn(response);

            mockMvc.perform(put("/manutencao/chamado/concluir/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 409 quando o chamado ainda não está em manutenção")
        void deveRetornar409QuandoAindaNaoEmManutencao() throws Exception {
            given(chamadoService.concluirChamado(1L))
                    .willThrow(new RecursoJaExistenteException("Somente chamados em manutenção podem ser concluídos"));

            mockMvc.perform(put("/manutencao/chamado/concluir/1"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("deve retornar 404 quando o chamado não existe")
        void deveRetornar404QuandoChamadoNaoExiste() throws Exception {
            given(chamadoService.concluirChamado(99L))
                    .willThrow(new NotFoundException("Chamado não encontrado, confira se o id está correto"));

            mockMvc.perform(put("/manutencao/chamado/concluir/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /manutencao/chamado/remover/{id}")
    class DeleteChamado {

        @Test
        @DisplayName("deve retornar 204 quando o chamado é deletado com sucesso")
        void deveRetornar204QuandoDeletadoComSucesso() throws Exception {
            Response response = Response.builder().status(204).message("Chamado deletado com sucesso").build();

            given(chamadoService.removerChamado(1L)).willReturn(response);

            mockMvc.perform(delete("/manutencao/chamado/remover/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve retornar 404 quando o chamado não existe")
        void deveRetornar404QuandoChamadoNaoExiste() throws Exception {
            given(chamadoService.removerChamado(99L))
                    .willThrow(new NotFoundException("Chamado não encontrado, para deletar, confira se o id está correto"));

            mockMvc.perform(delete("/manutencao/chamado/remover/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
