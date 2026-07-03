package com.aratechmoveis.manutencao.pecaestoque.repository;

import com.aratechmoveis.manutencao.pecaestoque.entity.PecaEstoque;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PecaEstoqueRepository")
class PecaEstoqueRepositoryTest {

    @Autowired
    private PecaEstoqueRepository pecaEstoqueRepository;

    private PecaEstoque umaPecaEstoque(String codigo) {
        return PecaEstoque.builder()
                .nome("Rolamento 6204")
                .codigo(codigo)
                .quantidade(15)
                .unidade("un")
                .localizacao("Prateleira A3")
                .build();
    }

    @Nested
    @DisplayName("existsByCodigoIgnoreCase")
    class ExistsByCodigoIgnoreCase {

        @Test
        @DisplayName("deve retornar true quando já existe peça com o código informado, ignorando case")
        void deveRetornarTrueQuandoCodigoExiste() {
            pecaEstoqueRepository.save(umaPecaEstoque("ROL-6204"));

            boolean existe = pecaEstoqueRepository.existsByCodigoIgnoreCase("rol-6204");

            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando não existe peça com o código informado")
        void deveRetornarFalseQuandoCodigoNaoExiste() {
            boolean existe = pecaEstoqueRepository.existsByCodigoIgnoreCase("ROL-INEXISTENTE");

            assertThat(existe).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByCodigoIgnoreCaseAndIdNot")
    class ExistsByCodigoIgnoreCaseAndIdNot {

        @Test
        @DisplayName("deve retornar true quando outra peça já usa o código informado")
        void deveRetornarTrueQuandoCodigoUsadoPorOutraPeca() {
            pecaEstoqueRepository.save(umaPecaEstoque("ROL-6204"));
            PecaEstoque outraPeca = pecaEstoqueRepository.save(umaPecaEstoque("ROL-6205"));

            boolean existe = pecaEstoqueRepository.existsByCodigoIgnoreCaseAndIdNot("ROL-6204", outraPeca.getId());

            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando o código pertence à própria peça")
        void deveRetornarFalseQuandoCodigoPertenceAPropriaPeca() {
            PecaEstoque peca = pecaEstoqueRepository.save(umaPecaEstoque("ROL-6204"));

            boolean existe = pecaEstoqueRepository.existsByCodigoIgnoreCaseAndIdNot("ROL-6204", peca.getId());

            assertThat(existe).isFalse();
        }
    }
}
