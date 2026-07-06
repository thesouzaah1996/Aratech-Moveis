package com.aratechmoveis.manutencao.pecaestoque.service.imp;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.exceptions.NotFoundException;
import com.aratechmoveis.manutencao.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.manutencao.pecaestoque.dto.PecaEstoqueDTO;
import com.aratechmoveis.manutencao.pecaestoque.entity.PecaEstoque;
import com.aratechmoveis.manutencao.pecaestoque.repository.PecaEstoqueRepository;
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
@DisplayName("PecaEstoqueServiceImp")
class PecaEstoqueServiceImpTest {

    @Mock
    private PecaEstoqueRepository pecaEstoqueRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PecaEstoqueServiceImp pecaEstoqueService;

    private PecaEstoqueDTO umaPecaEstoqueDTO() {
        PecaEstoqueDTO dto = new PecaEstoqueDTO();
        dto.setNome("Rolamento 6204");
        dto.setCodigo("ROL-6204");
        dto.setQuantidade(15);
        dto.setUnidade("un");
        dto.setLocalizacao("Prateleira A3");
        return dto;
    }

    private PecaEstoque umaPecaEstoque() {
        return PecaEstoque.builder()
                .id(1L)
                .nome("Rolamento 6204")
                .codigo("ROL-6204")
                .quantidade(15)
                .unidade("un")
                .localizacao("Prateleira A3")
                .build();
    }

    @Nested
    @DisplayName("addPecaEstoque")
    class AddPecaEstoque {

        @Test
        @DisplayName("deve criar peça com sucesso e retornar status 201")
        void deveCriarPecaComSucesso() {
            PecaEstoqueDTO dto = umaPecaEstoqueDTO();
            PecaEstoque peca = umaPecaEstoque();

            given(pecaEstoqueRepository.existsByCodigoIgnoreCase(dto.getCodigo())).willReturn(false);
            given(modelMapper.map(dto, PecaEstoque.class)).willReturn(peca);
            given(pecaEstoqueRepository.save(peca)).willReturn(peca);
            given(modelMapper.map(peca, PecaEstoqueDTO.class)).willReturn(dto);

            Response response = pecaEstoqueService.adicionarPecaEstoque(dto);

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getMessage()).isEqualTo("Peça cadastrada com sucesso");
            then(pecaEstoqueRepository).should().save(peca);
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando código já cadastrado")
        void deveLancarExcecaoQuandoCodigoDuplicado() {
            PecaEstoqueDTO dto = umaPecaEstoqueDTO();

            given(pecaEstoqueRepository.existsByCodigoIgnoreCase(dto.getCodigo())).willReturn(true);

            assertThatThrownBy(() -> pecaEstoqueService.adicionarPecaEstoque(dto))
                    .isInstanceOf(RecursoJaExistenteException.class)
                    .hasMessageContaining(dto.getCodigo());

            then(pecaEstoqueRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("updatePecaEstoque")
    class UpdatePecaEstoque {

        @Test
        @DisplayName("deve atualizar peça com sucesso e retornar status 200")
        void deveAtualizarPecaComSucesso() {
            PecaEstoque pecaExistente = umaPecaEstoque();
            PecaEstoqueDTO dto = new PecaEstoqueDTO();
            dto.setNome("Rolamento 6204 Reforçado");

            given(pecaEstoqueRepository.findById(1L)).willReturn(Optional.of(pecaExistente));
            given(pecaEstoqueRepository.save(pecaExistente)).willReturn(pecaExistente);
            given(modelMapper.map(pecaExistente, PecaEstoqueDTO.class)).willReturn(dto);

            Response response = pecaEstoqueService.atualizarPecaEstoque(1L, dto);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(pecaExistente.getNome()).isEqualTo("Rolamento 6204 Reforçado");
            then(pecaEstoqueRepository).should().save(pecaExistente);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando peça não encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            given(pecaEstoqueRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> pecaEstoqueService.atualizarPecaEstoque(99L, new PecaEstoqueDTO()))
                    .isInstanceOf(NotFoundException.class);

            then(pecaEstoqueRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve atualizar código quando informado e disponível")
        void deveAtualizarCodigoQuandoDisponivel() {
            PecaEstoque pecaExistente = umaPecaEstoque();
            PecaEstoqueDTO dto = new PecaEstoqueDTO();
            dto.setCodigo("ROL-6205");

            given(pecaEstoqueRepository.findById(1L)).willReturn(Optional.of(pecaExistente));
            given(pecaEstoqueRepository.existsByCodigoIgnoreCaseAndIdNot("ROL-6205", 1L)).willReturn(false);
            given(pecaEstoqueRepository.save(pecaExistente)).willReturn(pecaExistente);
            given(modelMapper.map(pecaExistente, PecaEstoqueDTO.class)).willReturn(dto);

            pecaEstoqueService.atualizarPecaEstoque(1L, dto);

            assertThat(pecaExistente.getCodigo()).isEqualTo("ROL-6205");
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando novo código já pertence a outra peça")
        void deveLancarExcecaoQuandoCodigoJaExiste() {
            PecaEstoque pecaExistente = umaPecaEstoque();
            PecaEstoqueDTO dto = new PecaEstoqueDTO();
            dto.setCodigo("ROL-9999");

            given(pecaEstoqueRepository.findById(1L)).willReturn(Optional.of(pecaExistente));
            given(pecaEstoqueRepository.existsByCodigoIgnoreCaseAndIdNot("ROL-9999", 1L)).willReturn(true);

            assertThatThrownBy(() -> pecaEstoqueService.atualizarPecaEstoque(1L, dto))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(pecaEstoqueRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve atualizar quantidade quando informada e válida")
        void deveAtualizarQuantidadeQuandoValida() {
            PecaEstoque pecaExistente = umaPecaEstoque();
            PecaEstoqueDTO dto = new PecaEstoqueDTO();
            dto.setQuantidade(30);

            given(pecaEstoqueRepository.findById(1L)).willReturn(Optional.of(pecaExistente));
            given(pecaEstoqueRepository.save(pecaExistente)).willReturn(pecaExistente);
            given(modelMapper.map(pecaExistente, PecaEstoqueDTO.class)).willReturn(dto);

            pecaEstoqueService.atualizarPecaEstoque(1L, dto);

            assertThat(pecaExistente.getQuantidade()).isEqualTo(30);
        }

        @Test
        @DisplayName("não deve alterar quantidade quando negativa")
        void naoDeveAlterarQuantidadeQuandoNegativa() {
            PecaEstoque pecaExistente = umaPecaEstoque();
            PecaEstoqueDTO dto = new PecaEstoqueDTO();
            dto.setQuantidade(-1);

            given(pecaEstoqueRepository.findById(1L)).willReturn(Optional.of(pecaExistente));
            given(pecaEstoqueRepository.save(pecaExistente)).willReturn(pecaExistente);
            given(modelMapper.map(pecaExistente, PecaEstoqueDTO.class)).willReturn(dto);

            pecaEstoqueService.atualizarPecaEstoque(1L, dto);

            assertThat(pecaExistente.getQuantidade()).isEqualTo(15);
        }

        @Test
        @DisplayName("não deve alterar nome quando nulo ou vazio")
        void naoDeveAlterarNomeQuandoNuloOuVazio() {
            PecaEstoque pecaExistente = umaPecaEstoque();
            PecaEstoqueDTO dto = new PecaEstoqueDTO();
            dto.setNome("  ");

            given(pecaEstoqueRepository.findById(1L)).willReturn(Optional.of(pecaExistente));
            given(pecaEstoqueRepository.save(pecaExistente)).willReturn(pecaExistente);
            given(modelMapper.map(pecaExistente, PecaEstoqueDTO.class)).willReturn(dto);

            pecaEstoqueService.atualizarPecaEstoque(1L, dto);

            assertThat(pecaExistente.getNome()).isEqualTo("Rolamento 6204");
        }
    }

    @Nested
    @DisplayName("getPecasEstoque")
    class GetPecasEstoque {

        @Test
        @DisplayName("deve retornar lista de peças com status 200")
        void deveRetornarListaDePecas() {
            List<PecaEstoque> pecas = List.of(umaPecaEstoque());
            List<PecaEstoqueDTO> pecasDTO = List.of(umaPecaEstoqueDTO());

            given(pecaEstoqueRepository.findAll(any(Sort.class))).willReturn(pecas);
            given(modelMapper.<List<PecaEstoqueDTO>>map(eq(pecas), any(Type.class))).willReturn(pecasDTO);

            Response response = pecaEstoqueService.listarPecasEstoque();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getPecasEstoque()).hasSize(1);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há peças cadastradas")
        void deveRetornarListaVaziaQuandoSemPecas() {
            given(pecaEstoqueRepository.findAll(any(Sort.class))).willReturn(List.of());
            given(modelMapper.<List<PecaEstoqueDTO>>map(any(), any(Type.class))).willReturn(List.of());

            Response response = pecaEstoqueService.listarPecasEstoque();

            assertThat(response.getPecasEstoque()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPecaEstoqueById")
    class GetPecaEstoqueById {

        @Test
        @DisplayName("deve retornar peça com sucesso quando id existe")
        void deveRetornarPecaComSucesso() {
            PecaEstoque peca = umaPecaEstoque();
            PecaEstoqueDTO dto = umaPecaEstoqueDTO();

            given(pecaEstoqueRepository.findById(1L)).willReturn(Optional.of(peca));
            given(modelMapper.map(peca, PecaEstoqueDTO.class)).willReturn(dto);

            Response response = pecaEstoqueService.buscarPecaEstoquePorId(1L);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getPecaEstoque()).isEqualTo(dto);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando peça não encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            given(pecaEstoqueRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> pecaEstoqueService.buscarPecaEstoquePorId(99L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deletePecaEstoque")
    class DeletePecaEstoque {

        @Test
        @DisplayName("deve deletar peça com sucesso e retornar status 204")
        void deveDeletarPecaComSucesso() {
            PecaEstoque peca = umaPecaEstoque();

            given(pecaEstoqueRepository.findById(1L)).willReturn(Optional.of(peca));

            Response response = pecaEstoqueService.removerPecaEstoque(1L);

            assertThat(response.getStatus()).isEqualTo(204);
            then(pecaEstoqueRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando peça não encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            given(pecaEstoqueRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> pecaEstoqueService.removerPecaEstoque(99L))
                    .isInstanceOf(NotFoundException.class);

            then(pecaEstoqueRepository).should(never()).deleteById(any());
        }
    }
}
