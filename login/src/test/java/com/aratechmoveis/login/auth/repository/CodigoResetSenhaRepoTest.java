package com.aratechmoveis.login.auth.repository;

import com.aratechmoveis.login.auth.entity.CodigoResetSenha;
import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.repository.FuncionarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("CodigoResetSenhaRepo")
class CodigoResetSenhaRepoTest {

    @Autowired
    private CodigoResetSenhaRepo codigoResetSenhaRepo;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    private Funcionario umFuncionario(Long idFuncionario, String emailPessoal) {
        Funcionario funcionario = new Funcionario();
        funcionario.setIdFuncionario(idFuncionario);
        funcionario.setNomeFuncionario("Maria Silva");
        funcionario.setEmailPessoal(emailPessoal);
        funcionario.setEmailCorporativo("maria@aratech.com");
        funcionario.setSenha("senha");
        return funcionarioRepository.save(funcionario);
    }

    private CodigoResetSenha umCodigo(Funcionario funcionario, String codigo) {
        return codigoResetSenhaRepo.save(CodigoResetSenha.builder()
                .codigo(codigo)
                .usado(false)
                .dataExpiracao(LocalDateTime.now().plusHours(5))
                .funcionario(funcionario)
                .build());
    }

    @Nested
    @DisplayName("findByCodigo")
    class FindByCodigo {

        @Test
        @DisplayName("deve retornar o código quando ele existe")
        void deveRetornarCodigoQuandoExiste() {
            Funcionario funcionario = umFuncionario(1L, "maria@gmail.com");
            umCodigo(funcionario, "ABC12");

            Optional<CodigoResetSenha> resultado = codigoResetSenhaRepo.findByCodigo("ABC12");

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getFuncionario().getIdFuncionario()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve retornar vazio quando o código não existe")
        void deveRetornarVazioQuandoNaoExiste() {
            Optional<CodigoResetSenha> resultado = codigoResetSenhaRepo.findByCodigo("INEXISTENTE");

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByFuncionarioIdFuncionario")
    class DeleteByFuncionarioIdFuncionario {

        @Test
        @DisplayName("deve remover os códigos vinculados ao funcionário")
        void deveRemoverCodigosDoFuncionario() {
            Funcionario funcionario = umFuncionario(2L, "joao@gmail.com");
            umCodigo(funcionario, "XYZ99");

            codigoResetSenhaRepo.deleteByFuncionarioIdFuncionario(2L);

            assertThat(codigoResetSenhaRepo.findByCodigo("XYZ99")).isEmpty();
        }
    }
}
