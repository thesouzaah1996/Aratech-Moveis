package com.aratechmoveis.almoxarifado.categoria.service.imp;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaDTO;
import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaLookupDTO;
import com.aratechmoveis.almoxarifado.categoria.modelo.Categoria;
import com.aratechmoveis.almoxarifado.categoria.repository.CategoriaRepository;
import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
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
@DisplayName("CategoriaServiceImp")
class CategoriaServiceImpTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoriaServiceImp categoriaService;

    private CategoriaDTO umaCategoriaDTO() {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setNome("Madeira");
        return dto;
    }

    private Categoria umaCategoria() {
        return Categoria.builder()
                .id(1L)
                .nome("Madeira")
                .build();
    }

    @Nested
    @DisplayName("criarCategoria")
    class CriarCategoria {

        @Test
        @DisplayName("deve criar categoria com sucesso e retornar status 201")
        void deveCriarCategoriaComSucesso() {
            CategoriaDTO dto = umaCategoriaDTO();
            Categoria categoria = umaCategoria();

            given(categoriaRepository.existsByNomeIgnoreCase(dto.getNome())).willReturn(false);
            given(modelMapper.map(dto, Categoria.class)).willReturn(categoria);
            given(categoriaRepository.save(categoria)).willReturn(categoria);
            given(modelMapper.map(categoria, CategoriaDTO.class)).willReturn(dto);

            Response response = categoriaService.criarCategoria(dto);

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getMessage()).isEqualTo("Categoria criada com sucesso");
            then(categoriaRepository).should().save(categoria);
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando nome já cadastrado")
        void deveLancarExcecaoQuandoNomeDuplicado() {
            CategoriaDTO dto = umaCategoriaDTO();

            given(categoriaRepository.existsByNomeIgnoreCase(dto.getNome())).willReturn(true);

            assertThatThrownBy(() -> categoriaService.criarCategoria(dto))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(categoriaRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCategorias")
    class GetCategorias {

        @Test
        @DisplayName("deve retornar lista de categorias com status 200")
        void deveRetornarListaDeCategorias() {
            List<Categoria> categorias = List.of(umaCategoria());
            List<CategoriaDTO> categoriasDTO = List.of(umaCategoriaDTO());

            given(categoriaRepository.findAll(any(Sort.class))).willReturn(categorias);
            given(modelMapper.map(eq(categorias), any(Type.class))).willReturn(categoriasDTO);

            Response response = categoriaService.getCategorias();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getCategorias()).hasSize(1);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há categorias cadastradas")
        void deveRetornarListaVaziaQuandoSemCategorias() {
            given(categoriaRepository.findAll(any(Sort.class))).willReturn(List.of());
            given(modelMapper.map(any(), any(Type.class))).willReturn(List.of());

            Response response = categoriaService.getCategorias();

            assertThat(response.getCategorias()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateCategoria")
    class UpdateCategoria {

        @Test
        @DisplayName("deve atualizar categoria com sucesso e retornar status 200")
        void deveAtualizarCategoriaComSucesso() {
            Categoria categoriaExistente = umaCategoria();
            CategoriaDTO dto = new CategoriaDTO();
            dto.setNome("Novo Nome");

            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoriaExistente));
            given(categoriaRepository.existsByNomeIgnoreCase("Novo Nome")).willReturn(false);
            given(categoriaRepository.save(categoriaExistente)).willReturn(categoriaExistente);
            given(modelMapper.map(categoriaExistente, CategoriaDTO.class)).willReturn(dto);

            Response response = categoriaService.updateCategoria(1L, dto);

            assertThat(response.getStatus()).isEqualTo(200);
            then(categoriaRepository).should().save(categoriaExistente);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando categoria não encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            given(categoriaRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoriaService.updateCategoria(99L, new CategoriaDTO()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando novo nome já pertence a outra categoria")
        void deveLancarExcecaoQuandoNovoNomeJaExiste() {
            Categoria categoriaExistente = umaCategoria();
            CategoriaDTO dto = new CategoriaDTO();
            dto.setNome("Metal");

            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoriaExistente));
            given(categoriaRepository.existsByNomeIgnoreCase("Metal")).willReturn(true);

            assertThatThrownBy(() -> categoriaService.updateCategoria(1L, dto))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(categoriaRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve permitir salvar quando o nome enviado é o mesmo da própria categoria")
        void devePermitirSalvarComMesmoNomeAtual() {
            Categoria categoriaExistente = umaCategoria();
            CategoriaDTO dto = new CategoriaDTO();
            dto.setNome("Madeira");

            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoriaExistente));
            given(categoriaRepository.existsByNomeIgnoreCase("Madeira")).willReturn(true);
            given(categoriaRepository.save(categoriaExistente)).willReturn(categoriaExistente);
            given(modelMapper.map(categoriaExistente, CategoriaDTO.class)).willReturn(dto);

            assertThatCode(() -> categoriaService.updateCategoria(1L, dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("não deve alterar o nome quando nome enviado é nulo ou vazio")
        void naoDeveAlterarNomeQuandoNuloOuVazio() {
            Categoria categoriaExistente = umaCategoria();
            CategoriaDTO dto = new CategoriaDTO();
            dto.setNome(null);

            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoriaExistente));
            given(categoriaRepository.save(categoriaExistente)).willReturn(categoriaExistente);
            given(modelMapper.map(categoriaExistente, CategoriaDTO.class)).willReturn(dto);

            categoriaService.updateCategoria(1L, dto);

            assertThat(categoriaExistente.getNome()).isEqualTo("Madeira");
        }
    }

    @Nested
    @DisplayName("deleteCategoria")
    class DeleteCategoria {

        @Test
        @DisplayName("deve deletar categoria com sucesso e retornar status 204")
        void deveDeletarCategoriaComSucesso() {
            Categoria categoria = umaCategoria();
            categoria.setProdutos(List.of());

            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoria));

            Response response = categoriaService.deleteCategoria(1L);

            assertThat(response.getStatus()).isEqualTo(204);
            then(categoriaRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando categoria não encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            given(categoriaRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoriaService.deleteCategoria(99L))
                    .isInstanceOf(NotFoundException.class);

            then(categoriaRepository).should(never()).deleteById(any());
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando categoria possui produtos vinculados")
        void deveLancarExcecaoQuandoCategoriaPossuiProdutos() {
            Categoria categoria = umaCategoria();
            categoria.setProdutos(List.of(new com.aratechmoveis.almoxarifado.produto.modelo.Produto()));

            given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoria));

            assertThatThrownBy(() -> categoriaService.deleteCategoria(1L))
                    .isInstanceOf(RecursoJaExistenteException.class)
                    .hasMessageContaining("produto(s) vinculado(s)");

            then(categoriaRepository).should(never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("lookupCategoria")
    class LookupCategoria {

        @Test
        @DisplayName("deve retornar lista de lookup com status 200")
        void deveRetornarLookupComSucesso() {
            List<Categoria> categorias = List.of(umaCategoria());

            given(categoriaRepository.findAll(any(Sort.class))).willReturn(categorias);

            Response response = categoriaService.lookupCategoria();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getCategoriaLookup()).hasSize(1);
            assertThat(response.getCategoriaLookup().get(0).getNome()).isEqualTo("Madeira");
        }

        @Test
        @DisplayName("deve retornar lista vazia de lookup quando não há categorias")
        void deveRetornarLookupVazioQuandoSemCategorias() {
            given(categoriaRepository.findAll(any(Sort.class))).willReturn(List.of());

            Response response = categoriaService.lookupCategoria();

            assertThat(response.getCategoriaLookup()).isEmpty();
        }
    }
}
