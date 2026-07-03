package com.aratechmoveis.almoxarifado.produto.repository;

import com.aratechmoveis.almoxarifado.produto.entity.Produto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ProdutoRepository")
class ProdutoRepositoryTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    private Produto umProduto(String sku, String localArmazenamento) {
        return Produto.builder()
                .nome("Cadeira de Escritório")
                .sku(sku)
                .quantidade(10)
                .localArmazenamento(localArmazenamento)
                .build();
    }

    @Nested
    @DisplayName("existsBySku")
    class ExistsBySku {

        @Test
        @DisplayName("deve retornar true quando já existe produto com o SKU informado")
        void deveRetornarTrueQuandoSkuExiste() {
            produtoRepository.save(umProduto("SKU-001", "prateleiraa01"));

            boolean existe = produtoRepository.existsBySku("SKU-001");

            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando não existe produto com o SKU informado")
        void deveRetornarFalseQuandoSkuNaoExiste() {
            boolean existe = produtoRepository.existsBySku("SKU-INEXISTENTE");

            assertThat(existe).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByLocalArmazenamentoAndIdNot")
    class ExistsByLocalArmazenamentoAndIdNot {

        @Test
        @DisplayName("deve retornar true quando outro produto já ocupa o local informado")
        void deveRetornarTrueQuandoLocalOcupadoPorOutroProduto() {
            produtoRepository.save(umProduto("SKU-001", "prateleiraa01"));
            Produto outroProduto = produtoRepository.save(umProduto("SKU-002", "prateleirab02"));

            boolean existe = produtoRepository.existsByLocalArmazenamentoAndIdNot("prateleiraa01", outroProduto.getId());

            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando o local pertence ao próprio produto")
        void deveRetornarFalseQuandoLocalPertenceAoProprioProduto() {
            Produto produto = produtoRepository.save(umProduto("SKU-001", "prateleiraa01"));

            boolean existe = produtoRepository.existsByLocalArmazenamentoAndIdNot("prateleiraa01", produto.getId());

            assertThat(existe).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando nenhum produto ocupa o local informado")
        void deveRetornarFalseQuandoLocalNaoOcupado() {
            Produto produto = produtoRepository.save(umProduto("SKU-001", "prateleiraa01"));

            boolean existe = produtoRepository.existsByLocalArmazenamentoAndIdNot("local-livre", produto.getId());

            assertThat(existe).isFalse();
        }
    }
}
