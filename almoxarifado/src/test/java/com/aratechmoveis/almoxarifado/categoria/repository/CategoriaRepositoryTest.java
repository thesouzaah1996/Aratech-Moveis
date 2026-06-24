//package com.aratechmoveis.almoxarifado.categoria.repository;
//
//import com.aratechmoveis.almoxarifado.categoria.modelo.Categoria;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
////@DataJpaTest
//@DisplayName("CategoriaRepository")
//class CategoriaRepositoryTest {
//
//    @Autowired
//    private CategoriaRepository categoriaRepository;
//
//    private Categoria umaCategoria() {
//        return Categoria.builder()
//                .nome("Madeira")
//                .build();
//    }
//
//    @Nested
//    @DisplayName("existsByNomeIgnoreCase")
//    class ExistsByNomeIgnoreCase {
//
//        @Test
//        @DisplayName("deve retornar true quando nome já está cadastrado")
//        void deveRetornarTrueQuandoNomeExiste() {
//            categoriaRepository.save(umaCategoria());
//
//            boolean existe = categoriaRepository.existsByNomeIgnoreCase("Madeira");
//
//            assertThat(existe).isTrue();
//        }
//
//        @Test
//        @DisplayName("deve retornar false quando nome não está cadastrado")
//        void deveRetornarFalseQuandoNomeNaoExiste() {
//            boolean existe = categoriaRepository.existsByNomeIgnoreCase("Inexistente");
//
//            assertThat(existe).isFalse();
//        }
//
//        @Test
//        @DisplayName("deve ser case-insensitive na busca por nome")
//        void deveSerCaseInsensitiveNaBuscaPorNome() {
//            categoriaRepository.save(umaCategoria());
//
//            boolean existe = categoriaRepository.existsByNomeIgnoreCase("MADEIRA");
//
//            assertThat(existe).isTrue();
//        }
//    }
//
//    @Nested
//    @DisplayName("save")
//    class Save {
//
//        @Test
//        @DisplayName("deve persistir categoria e gerar id automaticamente")
//        void devePersistirCategoriaEGerarId() {
//            Categoria salva = categoriaRepository.save(umaCategoria());
//
//            assertThat(salva.getId()).isNotNull();
//            assertThat(salva.getNome()).isEqualTo("Madeira");
//        }
//    }
//
//    @Nested
//    @DisplayName("findById")
//    class FindById {
//
//        @Test
//        @DisplayName("deve retornar categoria quando id existe")
//        void deveRetornarCategoriaQuandoIdExiste() {
//            Categoria salva = categoriaRepository.save(umaCategoria());
//
//            Optional<Categoria> encontrada = categoriaRepository.findById(salva.getId());
//
//            assertThat(encontrada).isPresent();
//            assertThat(encontrada.get().getNome()).isEqualTo("Madeira");
//        }
//
//        @Test
//        @DisplayName("deve retornar Optional vazio quando id não existe")
//        void deveRetornarVazioQuandoIdNaoExiste() {
//            Optional<Categoria> encontrada = categoriaRepository.findById(999L);
//
//            assertThat(encontrada).isEmpty();
//        }
//    }
//
//    @Nested
//    @DisplayName("deleteById")
//    class DeleteById {
//
//        @Test
//        @DisplayName("deve remover categoria quando id existe")
//        void deveRemoverCategoriaQuandoIdExiste() {
//            Categoria salva = categoriaRepository.save(umaCategoria());
//
//            categoriaRepository.deleteById(salva.getId());
//
//            assertThat(categoriaRepository.findById(salva.getId())).isEmpty();
//        }
//    }
//}
