package com.aratechmoveis.manutencao.solicitacaopeca.service.imp;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.chamado.entity.Prioridade;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.solicitacaopeca.dto.SolicitacaoPecaDTO;
import com.aratechmoveis.manutencao.solicitacaopeca.entity.FinalidadePeca;
import com.aratechmoveis.manutencao.solicitacaopeca.entity.SolicitacaoPeca;
import com.aratechmoveis.manutencao.solicitacaopeca.repository.SolicitacaoPecaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitacaoPecaServiceImp")
class SolicitacaoPecaServiceImpTest {

    @Mock
    private SolicitacaoPecaRepository solicitacaoPecaRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SolicitacaoPecaServiceImp solicitacaoPecaService;

    private SolicitacaoPecaDTO umaSolicitacaoPecaDTO() {
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

    private SolicitacaoPeca umaSolicitacaoPeca() {
        return SolicitacaoPeca.builder()
                .id(1L)
                .nomePeca("Correia dentada")
                .codigo("COR-100")
                .quantidade(2)
                .unidade("un")
                .equipamento("Torno CNC 03")
                .finalidade(FinalidadePeca.CORRETIVA)
                .prioridade(Prioridade.ALTA)
                .solicitante("Carlos Ferreira")
                .setor("Produção")
                .observacoes("Necessário para reparo urgente")
                .build();
    }

    @Nested
    @DisplayName("addSolicitacaoPeca")
    class AddSolicitacaoPeca {

        @Test
        @DisplayName("deve criar solicitação com sucesso e retornar status 201")
        void deveCriarSolicitacaoComSucesso() {
            SolicitacaoPecaDTO dto = umaSolicitacaoPecaDTO();
            SolicitacaoPeca solicitacao = umaSolicitacaoPeca();

            given(modelMapper.map(dto, SolicitacaoPeca.class)).willReturn(solicitacao);
            given(solicitacaoPecaRepository.save(solicitacao)).willReturn(solicitacao);
            given(modelMapper.map(solicitacao, SolicitacaoPecaDTO.class)).willReturn(dto);

            Response response = solicitacaoPecaService.adicionarSolicitacaoPeca(dto);

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getMessage()).isEqualTo("Solicitação de peça criada com sucesso");
            then(solicitacaoPecaRepository).should().save(solicitacao);
        }

        @Test
        @DisplayName("deve ignorar id enviado no DTO ao criar")
        void deveIgnorarIdEnviado() {
            SolicitacaoPecaDTO dto = umaSolicitacaoPecaDTO();
            dto.setId(999L);
            SolicitacaoPeca solicitacao = umaSolicitacaoPeca();
            solicitacao.setId(999L);

            given(modelMapper.map(dto, SolicitacaoPeca.class)).willReturn(solicitacao);
            given(solicitacaoPecaRepository.save(solicitacao)).willReturn(solicitacao);
            given(modelMapper.map(solicitacao, SolicitacaoPecaDTO.class)).willReturn(dto);

            solicitacaoPecaService.adicionarSolicitacaoPeca(dto);

            assertThat(solicitacao.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("getSolicitacoesPeca")
    class GetSolicitacoesPeca {

        @Test
        @DisplayName("deve retornar lista de solicitações com status 200")
        void deveRetornarListaDeSolicitacoes() {
            List<SolicitacaoPeca> solicitacoes = List.of(umaSolicitacaoPeca());
            List<SolicitacaoPecaDTO> solicitacoesDTO = List.of(umaSolicitacaoPecaDTO());

            given(solicitacaoPecaRepository.findAll(any(Sort.class))).willReturn(solicitacoes);
            given(modelMapper.<List<SolicitacaoPecaDTO>>map(eq(solicitacoes), any(Type.class))).willReturn(solicitacoesDTO);

            Response response = solicitacaoPecaService.listarSolicitacoesPeca();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getSolicitacoesPeca()).hasSize(1);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há solicitações cadastradas")
        void deveRetornarListaVaziaQuandoSemSolicitacoes() {
            given(solicitacaoPecaRepository.findAll(any(Sort.class))).willReturn(List.of());
            given(modelMapper.<List<SolicitacaoPecaDTO>>map(any(), any(Type.class))).willReturn(List.of());

            Response response = solicitacaoPecaService.listarSolicitacoesPeca();

            assertThat(response.getSolicitacoesPeca()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSolicitacaoPecaById")
    class GetSolicitacaoPecaById {

        @Test
        @DisplayName("deve retornar solicitação com sucesso quando id existe")
        void deveRetornarSolicitacaoComSucesso() {
            SolicitacaoPeca solicitacao = umaSolicitacaoPeca();
            SolicitacaoPecaDTO dto = umaSolicitacaoPecaDTO();

            given(solicitacaoPecaRepository.findById(1L)).willReturn(Optional.of(solicitacao));
            given(modelMapper.map(solicitacao, SolicitacaoPecaDTO.class)).willReturn(dto);

            Response response = solicitacaoPecaService.buscarSolicitacaoPecaPorId(1L);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getSolicitacaoPeca()).isEqualTo(dto);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando solicitação não encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            given(solicitacaoPecaRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> solicitacaoPecaService.buscarSolicitacaoPecaPorId(99L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteSolicitacaoPeca")
    class DeleteSolicitacaoPeca {

        @Test
        @DisplayName("deve deletar solicitação com sucesso e retornar status 204")
        void deveDeletarSolicitacaoComSucesso() {
            SolicitacaoPeca solicitacao = umaSolicitacaoPeca();

            given(solicitacaoPecaRepository.findById(1L)).willReturn(Optional.of(solicitacao));

            Response response = solicitacaoPecaService.removerSolicitacaoPeca(1L);

            assertThat(response.getStatus()).isEqualTo(204);
            then(solicitacaoPecaRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando solicitação não encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            given(solicitacaoPecaRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> solicitacaoPecaService.removerSolicitacaoPeca(99L))
                    .isInstanceOf(NotFoundException.class);

            then(solicitacaoPecaRepository).should(never()).deleteById(any());
        }
    }
}
