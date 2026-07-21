package com.aratechmoveis.login.funcionario.subscriber;

import com.aratechmoveis.login.funcionario.dto.FuncionarioDTO;
import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.mapper.FuncionarioMapper;
import com.aratechmoveis.login.funcionario.service.FuncionarioService;
import com.aratechmoveis.login.funcionario.subscriber.representation.FuncionarioRepresentation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("FuncionarioSubscriber")
class FuncionarioSubscriberTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private FuncionarioMapper funcionarioMapper;

    @Mock
    private FuncionarioService funcionarioService;

    @InjectMocks
    private FuncionarioSubscriber funcionarioSubscriber;

    @Nested
    @DisplayName("listenFuncionariosAdicionados")
    class ListenFuncionariosAdicionados {

        @Test
        @DisplayName("deve mapear e adicionar o funcionário com sucesso")
        void deveMapearEAdicionarComSucesso() {
            String json = "{\"idFuncionario\":1}";
            FuncionarioRepresentation representation = new FuncionarioRepresentation(
                    1L, "Maria Silva", "maria@gmail.com", "maria@aratech.com", List.of(), true);
            Funcionario funcionario = new Funcionario();
            FuncionarioDTO funcionarioDTO = new FuncionarioDTO();

            given(objectMapper.readValue(json, FuncionarioRepresentation.class)).willReturn(representation);
            given(funcionarioMapper.map(representation)).willReturn(funcionario);
            given(modelMapper.map(funcionario, FuncionarioDTO.class)).willReturn(funcionarioDTO);

            funcionarioSubscriber.listenFuncionariosAdicionados(json);

            then(funcionarioService).should().adicionarFuncionario(funcionarioDTO);
        }

        @Test
        @DisplayName("não deve propagar exceção quando ocorre erro na consumação")
        void naoDevePropagarExcecaoQuandoOcorreErro() {
            String json = "json-invalido";

            willThrow(new RuntimeException("erro de parsing")).given(objectMapper).readValue(json, FuncionarioRepresentation.class);

            assertThatCode(() -> funcionarioSubscriber.listenFuncionariosAdicionados(json))
                    .doesNotThrowAnyException();

            then(funcionarioService).should(never()).adicionarFuncionario(any());
        }
    }
}
