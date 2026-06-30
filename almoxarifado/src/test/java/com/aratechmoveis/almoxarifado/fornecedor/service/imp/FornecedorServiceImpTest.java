package com.aratechmoveis.almoxarifado.fornecedor.service.imp;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorDTO;
import com.aratechmoveis.almoxarifado.fornecedor.model.Fornecedor;
import com.aratechmoveis.almoxarifado.fornecedor.repository.FornecedorRepository;
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
@DisplayName("FornecedorServiceImp")
class FornecedorServiceImpTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private FornecedorServiceImp fornecedorService;

    private FornecedorDTO umFornecedorDTO() {
        FornecedorDTO dto = new FornecedorDTO();
        dto.setNome("Fornecedor Teste");
        dto.setEmail("teste@email.com");
        dto.setTelefone("(11) 1234-5678");
        return dto;
    }

    private Fornecedor umFornecedor() {
        return Fornecedor.builder()
                .id(1L)
                .nome("Fornecedor Teste")
                .email("teste@email.com")
                .telefone("(11) 1234-5678")
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("addFornecedor")
    class AddFornecedor {

        @Test
        @DisplayName("deve criar fornecedor com sucesso e retornar status 201")
        void deveCriarFornecedorComSucesso() {
            FornecedorDTO dto = umFornecedorDTO();
            Fornecedor fornecedor = umFornecedor();

            given(fornecedorRepository.existsByEmail(dto.getEmail())).willReturn(false);
            given(modelMapper.map(dto, Fornecedor.class)).willReturn(fornecedor);
            given(fornecedorRepository.save(fornecedor)).willReturn(fornecedor);
            given(modelMapper.map(fornecedor, FornecedorDTO.class)).willReturn(dto);

            Response response = fornecedorService.addFornecedor(dto);

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getMessage()).isEqualTo("Fornecedor adicionado com sucesso");
            then(fornecedorRepository).should().save(fornecedor);
        }

        @Test
        @DisplayName("deve normalizar e-mail para minúsculo e sem espaços antes de validar")
        void deveNormalizarEmailParaMinusculo() {
            FornecedorDTO dto = umFornecedorDTO();
            dto.setEmail("  TESTE@EMAIL.COM  ");
            Fornecedor fornecedor = umFornecedor();

            given(fornecedorRepository.existsByEmail("teste@email.com")).willReturn(false);
            given(modelMapper.map(dto, Fornecedor.class)).willReturn(fornecedor);
            given(fornecedorRepository.save(fornecedor)).willReturn(fornecedor);
            given(modelMapper.map(fornecedor, FornecedorDTO.class)).willReturn(dto);

            fornecedorService.addFornecedor(dto);

            assertThat(dto.getEmail()).isEqualTo("teste@email.com");
        }

        @Test
        @DisplayName("deve setar ativo como true independente do que o ModelMapper mapear")
        void deveSetarAtivoComoTrue() {
            FornecedorDTO dto = umFornecedorDTO();
            Fornecedor fornecedor = umFornecedor();
            fornecedor.setAtivo(null);

            given(fornecedorRepository.existsByEmail(dto.getEmail())).willReturn(false);
            given(modelMapper.map(dto, Fornecedor.class)).willReturn(fornecedor);
            given(fornecedorRepository.save(any())).willReturn(fornecedor);
            given(modelMapper.map(fornecedor, FornecedorDTO.class)).willReturn(dto);

            fornecedorService.addFornecedor(dto);

            assertThat(fornecedor.getAtivo()).isTrue();
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando e-mail já cadastrado")
        void deveLancarExcecaoQuandoEmailDuplicado() {
            FornecedorDTO dto = umFornecedorDTO();

            given(fornecedorRepository.existsByEmail(dto.getEmail())).willReturn(true);

            assertThatThrownBy(() -> fornecedorService.addFornecedor(dto))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(fornecedorRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateFornecedor")
    class UpdateFornecedor {

        @Test
        @DisplayName("deve atualizar fornecedor com sucesso e retornar status 200")
        void deveAtualizarFornecedorComSucesso() {
            Fornecedor fornecedorExistente = umFornecedor();
            FornecedorDTO dto = new FornecedorDTO();
            dto.setNome("Novo Nome");

            given(fornecedorRepository.findById(1L)).willReturn(Optional.of(fornecedorExistente));
            given(fornecedorRepository.save(fornecedorExistente)).willReturn(fornecedorExistente);
            given(modelMapper.map(fornecedorExistente, FornecedorDTO.class)).willReturn(dto);

            Response response = fornecedorService.updateFornecedor(1L, dto);

            assertThat(response.getStatus()).isEqualTo(200);
            then(fornecedorRepository).should().save(fornecedorExistente);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando fornecedor não existe")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(fornecedorRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> fornecedorService.updateFornecedor(99L, new FornecedorDTO()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando novo e-mail já pertence a outro fornecedor")
        void deveLancarExcecaoQuandoNovoEmailJaExiste() {
            Fornecedor fornecedorExistente = umFornecedor();
            FornecedorDTO dto = new FornecedorDTO();
            dto.setEmail("outro@email.com");

            given(fornecedorRepository.findById(1L)).willReturn(Optional.of(fornecedorExistente));
            given(fornecedorRepository.existsByEmail("outro@email.com")).willReturn(true);

            assertThatThrownBy(() -> fornecedorService.updateFornecedor(1L, dto))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(fornecedorRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve permitir salvar quando o e-mail enviado é o mesmo do próprio fornecedor")
        void devePermitirSalvarComMesmoEmailAtual() {
            Fornecedor fornecedorExistente = umFornecedor();
            FornecedorDTO dto = new FornecedorDTO();
            dto.setEmail("teste@email.com");

            given(fornecedorRepository.findById(1L)).willReturn(Optional.of(fornecedorExistente));
            given(fornecedorRepository.save(fornecedorExistente)).willReturn(fornecedorExistente);
            given(modelMapper.map(fornecedorExistente, FornecedorDTO.class)).willReturn(dto);

            assertThatCode(() -> fornecedorService.updateFornecedor(1L, dto))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getFornecedores")
    class GetFornecedores {

        @Test
        @DisplayName("deve retornar lista de fornecedores com status 200")
        void deveRetornarListaDeFornecedores() {
            List<Fornecedor> fornecedores = List.of(umFornecedor());
            List<FornecedorDTO> fornecedoresDTO = List.of(umFornecedorDTO());

            given(fornecedorRepository.findAll(any(Sort.class))).willReturn(fornecedores);
            given(modelMapper.map(eq(fornecedores), any(Type.class))).willReturn(fornecedoresDTO);

            Response response = fornecedorService.getFornecedores();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getFornecedores()).hasSize(1);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há fornecedores cadastrados")
        void deveRetornarListaVaziaQuandoSemFornecedores() {
            given(fornecedorRepository.findAll(any(Sort.class))).willReturn(List.of());
            given(modelMapper.map(any(), any(Type.class))).willReturn(List.of());

            Response response = fornecedorService.getFornecedores();

            assertThat(response.getFornecedores()).isEmpty();
        }
    }

    @Nested
    @DisplayName("disableFornecedor")
    class DisableFornecedor {

        @Test
        @DisplayName("deve desativar fornecedor e setar ativo como false")
        void deveDesativarFornecedorComSucesso() {
            Fornecedor fornecedor = umFornecedor();

            given(fornecedorRepository.findById(1L)).willReturn(Optional.of(fornecedor));

            Response response = fornecedorService.disableFornecedor(1L);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(fornecedor.getAtivo()).isFalse();
            then(fornecedorRepository).should().save(fornecedor);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando fornecedor não existe")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(fornecedorRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> fornecedorService.disableFornecedor(99L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("enableFornecedor")
    class EnableFornecedor {

        @Test
        @DisplayName("deve reativar fornecedor e setar ativo como true")
        void deveReativarFornecedorComSucesso() {
            Fornecedor fornecedor = umFornecedor();
            fornecedor.setAtivo(false);

            given(fornecedorRepository.findById(1L)).willReturn(Optional.of(fornecedor));

            Response response = fornecedorService.enableFornecedor(1L);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(fornecedor.getAtivo()).isTrue();
            then(fornecedorRepository).should().save(fornecedor);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando fornecedor não existe")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(fornecedorRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> fornecedorService.enableFornecedor(99L))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
