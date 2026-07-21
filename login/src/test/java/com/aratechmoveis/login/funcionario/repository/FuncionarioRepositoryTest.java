package com.aratechmoveis.login.funcionario.repository;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("FuncionarioRepository")
class FuncionarioRepositoryTest {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    private Funcionario umFuncionario(Long idFuncionario, String emailPessoal, String emailCorporativo) {
        Funcionario funcionario = new Funcionario();
        funcionario.setIdFuncionario(idFuncionario);
        funcionario.setNomeFuncionario("Maria Silva");
        funcionario.setEmailPessoal(emailPessoal);
        funcionario.setEmailCorporativo(emailCorporativo);
        funcionario.setSenha("senha");
        return funcionario;
    }

    @Nested
    @DisplayName("existsByIdFuncionario")
    class ExistsByIdFuncionario {

        @Test
        @DisplayName("deve retornar true quando o funcionário existe")
        void deveRetornarTrueQuandoExiste() {
            funcionarioRepository.save(umFuncionario(1L, "maria@gmail.com", "maria@aratech.com"));

            assertThat(funcionarioRepository.existsByIdFuncionario(1L)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando o funcionário não existe")
        void deveRetornarFalseQuandoNaoExiste() {
            assertThat(funcionarioRepository.existsByIdFuncionario(99L)).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByEmailCorporativo")
    class ExistsByEmailCorporativo {

        @Test
        @DisplayName("deve retornar true quando o email corporativo existe")
        void deveRetornarTrueQuandoExiste() {
            funcionarioRepository.save(umFuncionario(2L, "joao@gmail.com", "joao@aratech.com"));

            assertThat(funcionarioRepository.existsByEmailCorporativo("joao@aratech.com")).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando o email corporativo não existe")
        void deveRetornarFalseQuandoNaoExiste() {
            assertThat(funcionarioRepository.existsByEmailCorporativo("inexistente@aratech.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("findByEmailCorporativo")
    class FindByEmailCorporativo {

        @Test
        @DisplayName("deve retornar o funcionário quando o email corporativo existe")
        void deveRetornarFuncionarioQuandoExiste() {
            funcionarioRepository.save(umFuncionario(3L, "carlos@gmail.com", "carlos@aratech.com"));

            Optional<Funcionario> resultado = funcionarioRepository.findByEmailCorporativo("carlos@aratech.com");

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getIdFuncionario()).isEqualTo(3L);
        }

        @Test
        @DisplayName("deve retornar vazio quando o email corporativo não existe")
        void deveRetornarVazioQuandoNaoExiste() {
            assertThat(funcionarioRepository.findByEmailCorporativo("inexistente@aratech.com")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmailPessoal")
    class FindByEmailPessoal {

        @Test
        @DisplayName("deve retornar o funcionário quando o email pessoal existe")
        void deveRetornarFuncionarioQuandoExiste() {
            funcionarioRepository.save(umFuncionario(4L, "ana@gmail.com", "ana@aratech.com"));

            Optional<Funcionario> resultado = funcionarioRepository.findByEmailPessoal("ana@gmail.com");

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getIdFuncionario()).isEqualTo(4L);
        }

        @Test
        @DisplayName("deve retornar vazio quando o email pessoal não existe")
        void deveRetornarVazioQuandoNaoExiste() {
            assertThat(funcionarioRepository.findByEmailPessoal("inexistente@gmail.com")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdFuncionario")
    class FindByIdFuncionario {

        @Test
        @DisplayName("deve retornar o funcionário quando o id existe")
        void deveRetornarFuncionarioQuandoExiste() {
            funcionarioRepository.save(umFuncionario(5L, "paulo@gmail.com", "paulo@aratech.com"));

            Optional<Funcionario> resultado = funcionarioRepository.findByIdFuncionario(5L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getEmailPessoal()).isEqualTo("paulo@gmail.com");
        }

        @Test
        @DisplayName("deve retornar vazio quando o id não existe")
        void deveRetornarVazioQuandoNaoExiste() {
            assertThat(funcionarioRepository.findByIdFuncionario(999L)).isEmpty();
        }
    }
}
