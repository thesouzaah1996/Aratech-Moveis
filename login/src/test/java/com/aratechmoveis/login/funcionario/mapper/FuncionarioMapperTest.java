package com.aratechmoveis.login.funcionario.mapper;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.entity.Perfil;
import com.aratechmoveis.login.funcionario.subscriber.representation.FuncionarioRepresentation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FuncionarioMapper")
class FuncionarioMapperTest {

    private final FuncionarioMapper funcionarioMapper = new FuncionarioMapper();

    @Nested
    @DisplayName("map")
    class Map {

        @Test
        @DisplayName("deve copiar todos os campos da representation para o funcionário")
        void deveCopiarTodosOsCampos() {
            List<Perfil> perfis = List.of(Perfil.builder().nome("ADMIN").ativo(true).build());
            FuncionarioRepresentation representation = new FuncionarioRepresentation(
                    1L, "Maria Silva", "maria@gmail.com", "maria@aratech.com", perfis, true);

            Funcionario funcionario = funcionarioMapper.map(representation);

            assertThat(funcionario.getIdFuncionario()).isEqualTo(1L);
            assertThat(funcionario.getNomeFuncionario()).isEqualTo("Maria Silva");
            assertThat(funcionario.getEmailPessoal()).isEqualTo("maria@gmail.com");
            assertThat(funcionario.getEmailCorporativo()).isEqualTo("maria@aratech.com");
            assertThat(funcionario.getPerfis()).isEqualTo(perfis);
            assertThat(funcionario.isAtivo()).isTrue();
        }

        @Test
        @DisplayName("deve sempre forçar primeiroLogin como true")
        void deveForcarPrimeiroLoginComoTrue() {
            FuncionarioRepresentation representation = new FuncionarioRepresentation(
                    2L, "João Souza", "joao@gmail.com", "joao@aratech.com", List.of(), false);

            Funcionario funcionario = funcionarioMapper.map(representation);

            assertThat(funcionario.isPrimeiroLogin()).isTrue();
        }
    }
}
