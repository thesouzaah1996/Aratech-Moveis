package com.aratechmoveis.login.funcionario.repository;

import com.aratechmoveis.login.funcionario.entity.Perfil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PerfilRepository")
class PerfilRepositoryTest {

    @Autowired
    private PerfilRepository perfilRepository;

    @Nested
    @DisplayName("findByNome")
    class FindByNome {

        @Test
        @DisplayName("deve retornar o perfil quando o nome existe")
        void deveRetornarPerfilQuandoExiste() {
            perfilRepository.save(Perfil.builder().nome("ADMIN").ativo(true).build());

            Optional<Perfil> resultado = perfilRepository.findByNome("ADMIN");

            assertThat(resultado).isPresent();
            assertThat(resultado.get().isAtivo()).isTrue();
        }

        @Test
        @DisplayName("deve retornar vazio quando o nome não existe")
        void deveRetornarVazioQuandoNaoExiste() {
            assertThat(perfilRepository.findByNome("INEXISTENTE")).isEmpty();
        }
    }
}
