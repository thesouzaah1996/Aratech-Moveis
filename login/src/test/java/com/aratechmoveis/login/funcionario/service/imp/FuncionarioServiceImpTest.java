package com.aratechmoveis.login.funcionario.service.imp;

import com.aratechmoveis.login.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.login.funcionario.dto.FuncionarioDTO;
import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.entity.Perfil;
import com.aratechmoveis.login.funcionario.repository.FuncionarioRepository;
import com.aratechmoveis.login.funcionario.repository.PerfilRepository;
import com.aratechmoveis.login.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("FuncionarioServiceImp")
class FuncionarioServiceImpTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private FuncionarioServiceImp funcionarioService;

    private FuncionarioDTO umFuncionarioDTO() {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setIdFuncionario(1L);
        dto.setNomeFuncionario("Maria Silva");
        dto.setEmailCorporativo("maria@aratech.com");
        dto.setPerfis(List.of(Perfil.builder().nome("ADMIN").ativo(true).build()));
        dto.setAtivo(true);
        return dto;
    }

    @Nested
    @DisplayName("adicionarFuncionario")
    class AdicionarFuncionario {

        @Test
        @DisplayName("deve reaproveitar perfil já existente ao adicionar funcionário")
        void deveReaproveitarPerfilExistente() {
            FuncionarioDTO dto = umFuncionarioDTO();
            Perfil perfilExistente = Perfil.builder().id(5L).nome("ADMIN").ativo(true).build();
            Funcionario funcionarioMapeado = new Funcionario();

            given(funcionarioRepository.existsByIdFuncionario(1L)).willReturn(false);
            given(funcionarioRepository.existsByEmailCorporativo("maria@aratech.com")).willReturn(false);
            given(perfilRepository.findByNome("ADMIN")).willReturn(Optional.of(perfilExistente));
            given(modelMapper.map(dto, Funcionario.class)).willReturn(funcionarioMapeado);

            Response response = funcionarioService.adicionarFuncionario(dto);

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(dto.getPerfis()).containsExactly(perfilExistente);
            then(perfilRepository).should(never()).save(any());
            then(funcionarioRepository).should().save(funcionarioMapeado);
        }

        @Test
        @DisplayName("deve criar um novo perfil quando ele ainda não existe")
        void deveCriarNovoPerfilQuandoNaoExiste() {
            FuncionarioDTO dto = umFuncionarioDTO();
            Funcionario funcionarioMapeado = new Funcionario();

            given(funcionarioRepository.existsByIdFuncionario(1L)).willReturn(false);
            given(funcionarioRepository.existsByEmailCorporativo("maria@aratech.com")).willReturn(false);
            given(perfilRepository.findByNome("ADMIN")).willReturn(Optional.empty());
            given(perfilRepository.save(any(Perfil.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(modelMapper.map(dto, Funcionario.class)).willReturn(funcionarioMapeado);

            Response response = funcionarioService.adicionarFuncionario(dto);

            assertThat(response.getStatus()).isEqualTo(201);
            then(perfilRepository).should().save(any(Perfil.class));
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando o idFuncionario já existe")
        void deveLancarExcecaoQuandoIdFuncionarioJaExiste() {
            FuncionarioDTO dto = umFuncionarioDTO();

            given(funcionarioRepository.existsByIdFuncionario(1L)).willReturn(true);

            assertThatThrownBy(() -> funcionarioService.adicionarFuncionario(dto))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(funcionarioRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando o email corporativo já existe")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            FuncionarioDTO dto = umFuncionarioDTO();

            given(funcionarioRepository.existsByIdFuncionario(1L)).willReturn(false);
            given(funcionarioRepository.existsByEmailCorporativo("maria@aratech.com")).willReturn(true);

            assertThatThrownBy(() -> funcionarioService.adicionarFuncionario(dto))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(funcionarioRepository).should(never()).save(any());
        }
    }
}
