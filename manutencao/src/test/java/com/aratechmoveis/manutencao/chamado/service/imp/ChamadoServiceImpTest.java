package com.aratechmoveis.manutencao.chamado.service.imp;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.chamado.dto.AtribuirMecanicoDTO;
import com.aratechmoveis.manutencao.chamado.dto.ChamadoDTO;
import com.aratechmoveis.manutencao.chamado.entity.Chamado;
import com.aratechmoveis.manutencao.chamado.entity.Prioridade;
import com.aratechmoveis.manutencao.chamado.entity.StatusChamado;
import com.aratechmoveis.manutencao.chamado.entity.TipoManutencao;
import com.aratechmoveis.manutencao.chamado.repository.ChamadoRepository;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.exceptions.RecursoJaExistenteException;
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
@DisplayName("ChamadoServiceImp")
class ChamadoServiceImpTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ChamadoServiceImp chamadoService;

    private ChamadoDTO umChamadoDTO() {
        ChamadoDTO dto = new ChamadoDTO();
        dto.setEquipamento("Torno CNC 03");
        dto.setTipo(TipoManutencao.CORRETIVA);
        dto.setPrioridade(Prioridade.ALTA);
        dto.setSolicitante("Carlos Ferreira");
        dto.setSetor("Produção");
        dto.setDescricao("Equipamento com falha no motor");
        return dto;
    }

    private Chamado umChamado() {
        return Chamado.builder()
                .id(1L)
                .equipamento("Torno CNC 03")
                .tipo(TipoManutencao.CORRETIVA)
                .prioridade(Prioridade.ALTA)
                .solicitante("Carlos Ferreira")
                .setor("Produção")
                .descricao("Equipamento com falha no motor")
                .status(StatusChamado.ABERTA)
                .build();
    }

    @Nested
    @DisplayName("addChamado")
    class AddChamado {

        @Test
        @DisplayName("deve criar chamado com sucesso e retornar status 201")
        void deveCriarChamadoComSucesso() {
            ChamadoDTO dto = umChamadoDTO();
            Chamado chamado = umChamado();

            given(modelMapper.map(dto, Chamado.class)).willReturn(chamado);
            given(chamadoRepository.save(chamado)).willReturn(chamado);
            given(modelMapper.map(chamado, ChamadoDTO.class)).willReturn(dto);

            Response response = chamadoService.adicionarChamado(dto);

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getMessage()).isEqualTo("Chamado de manutenção criado com sucesso");
            then(chamadoRepository).should().save(chamado);
        }

        @Test
        @DisplayName("deve garantir status ABERTA e mecânico nulo mesmo que o mapeamento traga outro valor")
        void deveForcarStatusAbertaEMecanicoNulo() {
            ChamadoDTO dto = umChamadoDTO();
            Chamado chamado = umChamado();
            chamado.setStatus(StatusChamado.CONCLUIDA);
            chamado.setMecanico("Alguém");

            given(modelMapper.map(dto, Chamado.class)).willReturn(chamado);
            given(chamadoRepository.save(chamado)).willReturn(chamado);
            given(modelMapper.map(chamado, ChamadoDTO.class)).willReturn(dto);

            chamadoService.adicionarChamado(dto);

            assertThat(chamado.getStatus()).isEqualTo(StatusChamado.ABERTA);
            assertThat(chamado.getMecanico()).isNull();
        }

        @Test
        @DisplayName("deve ignorar id enviado no DTO ao criar")
        void deveIgnorarIdEnviado() {
            ChamadoDTO dto = umChamadoDTO();
            dto.setId(999L);
            Chamado chamado = umChamado();
            chamado.setId(999L);

            given(modelMapper.map(dto, Chamado.class)).willReturn(chamado);
            given(chamadoRepository.save(chamado)).willReturn(chamado);
            given(modelMapper.map(chamado, ChamadoDTO.class)).willReturn(dto);

            chamadoService.adicionarChamado(dto);

            assertThat(chamado.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("getChamados")
    class GetChamados {

        @Test
        @DisplayName("deve retornar todos os chamados quando status não é informado")
        void deveRetornarTodosOsChamados() {
            List<Chamado> chamados = List.of(umChamado());
            List<ChamadoDTO> chamadosDTO = List.of(umChamadoDTO());

            given(chamadoRepository.findAll(any(Sort.class))).willReturn(chamados);
            given(modelMapper.<List<ChamadoDTO>>map(eq(chamados), any(Type.class))).willReturn(chamadosDTO);

            Response response = chamadoService.listarChamados(null);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getChamados()).hasSize(1);
            then(chamadoRepository).should(never()).findByStatus(any(), any());
        }

        @Test
        @DisplayName("deve retornar chamados filtrados por status quando informado")
        void deveRetornarChamadosFiltradosPorStatus() {
            List<Chamado> chamados = List.of(umChamado());
            List<ChamadoDTO> chamadosDTO = List.of(umChamadoDTO());

            given(chamadoRepository.findByStatus(eq(StatusChamado.ABERTA), any(Sort.class))).willReturn(chamados);
            given(modelMapper.<List<ChamadoDTO>>map(eq(chamados), any(Type.class))).willReturn(chamadosDTO);

            Response response = chamadoService.listarChamados(StatusChamado.ABERTA);

            assertThat(response.getChamados()).hasSize(1);
            then(chamadoRepository).should(never()).findAll(any(Sort.class));
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há chamados")
        void deveRetornarListaVaziaQuandoSemChamados() {
            given(chamadoRepository.findAll(any(Sort.class))).willReturn(List.of());
            given(modelMapper.<List<ChamadoDTO>>map(any(), any(Type.class))).willReturn(List.of());

            Response response = chamadoService.listarChamados(null);

            assertThat(response.getChamados()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getChamadoById")
    class GetChamadoById {

        @Test
        @DisplayName("deve retornar chamado com sucesso quando id existe")
        void deveRetornarChamadoComSucesso() {
            Chamado chamado = umChamado();
            ChamadoDTO dto = umChamadoDTO();

            given(chamadoRepository.findById(1L)).willReturn(Optional.of(chamado));
            given(modelMapper.map(chamado, ChamadoDTO.class)).willReturn(dto);

            Response response = chamadoService.buscarChamadoPorId(1L);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getChamado()).isEqualTo(dto);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando chamado não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(chamadoRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> chamadoService.buscarChamadoPorId(99L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("atribuirMecanico")
    class AtribuirMecanico {

        @Test
        @DisplayName("deve atribuir mecânico e mudar status para EM_MANUTENCAO")
        void deveAtribuirMecanicoComSucesso() {
            Chamado chamado = umChamado();
            AtribuirMecanicoDTO dto = new AtribuirMecanicoDTO("Joao Pedro");

            given(chamadoRepository.findById(1L)).willReturn(Optional.of(chamado));
            given(chamadoRepository.save(chamado)).willReturn(chamado);
            given(modelMapper.map(chamado, ChamadoDTO.class)).willReturn(umChamadoDTO());

            Response response = chamadoService.atribuirMecanico(1L, dto);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(chamado.getMecanico()).isEqualTo("Joao Pedro");
            assertThat(chamado.getStatus()).isEqualTo(StatusChamado.EM_MANUTENCAO);
            then(chamadoRepository).should().save(chamado);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando chamado não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(chamadoRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> chamadoService.atribuirMecanico(99L, new AtribuirMecanicoDTO("Joao")))
                    .isInstanceOf(NotFoundException.class);

            then(chamadoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando chamado já está concluído")
        void deveLancarExcecaoQuandoJaConcluido() {
            Chamado chamado = umChamado();
            chamado.setStatus(StatusChamado.CONCLUIDA);

            given(chamadoRepository.findById(1L)).willReturn(Optional.of(chamado));

            assertThatThrownBy(() -> chamadoService.atribuirMecanico(1L, new AtribuirMecanicoDTO("Joao")))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(chamadoRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("concluirChamado")
    class ConcluirChamado {

        @Test
        @DisplayName("deve concluir chamado com sucesso quando está em manutenção")
        void deveConcluirChamadoComSucesso() {
            Chamado chamado = umChamado();
            chamado.setStatus(StatusChamado.EM_MANUTENCAO);

            given(chamadoRepository.findById(1L)).willReturn(Optional.of(chamado));
            given(chamadoRepository.save(chamado)).willReturn(chamado);
            given(modelMapper.map(chamado, ChamadoDTO.class)).willReturn(umChamadoDTO());

            Response response = chamadoService.concluirChamado(1L);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(chamado.getStatus()).isEqualTo(StatusChamado.CONCLUIDA);
            then(chamadoRepository).should().save(chamado);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando chamado não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(chamadoRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> chamadoService.concluirChamado(99L))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando chamado ainda está aberto")
        void deveLancarExcecaoQuandoAindaAberto() {
            Chamado chamado = umChamado();
            chamado.setStatus(StatusChamado.ABERTA);

            given(chamadoRepository.findById(1L)).willReturn(Optional.of(chamado));

            assertThatThrownBy(() -> chamadoService.concluirChamado(1L))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(chamadoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando chamado já está concluído")
        void deveLancarExcecaoQuandoJaConcluido() {
            Chamado chamado = umChamado();
            chamado.setStatus(StatusChamado.CONCLUIDA);

            given(chamadoRepository.findById(1L)).willReturn(Optional.of(chamado));

            assertThatThrownBy(() -> chamadoService.concluirChamado(1L))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(chamadoRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteChamado")
    class DeleteChamado {

        @Test
        @DisplayName("deve deletar chamado com sucesso e retornar status 204")
        void deveDeletarChamadoComSucesso() {
            Chamado chamado = umChamado();

            given(chamadoRepository.findById(1L)).willReturn(Optional.of(chamado));

            Response response = chamadoService.removerChamado(1L);

            assertThat(response.getStatus()).isEqualTo(204);
            then(chamadoRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando chamado não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(chamadoRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> chamadoService.removerChamado(99L))
                    .isInstanceOf(NotFoundException.class);

            then(chamadoRepository).should(never()).deleteById(any());
        }
    }
}
