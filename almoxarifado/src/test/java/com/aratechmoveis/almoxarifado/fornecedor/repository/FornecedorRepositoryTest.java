//package com.aratechmoveis.almoxarifado.fornecedor.repository;
//
//import com.aratechmoveis.almoxarifado.fornecedor.model.Fornecedor;
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
//@DisplayName("FornecedorRepository")
//class FornecedorRepositoryTest {
//
//    @Autowired
//    private FornecedorRepository fornecedorRepository;
//
//    private Fornecedor umFornecedor() {
//        return Fornecedor.builder()
//                .nome("Fornecedor Teste")
//                .email("teste@email.com")
//                .telefone("(11) 1234-5678")
//                .ativo(true)
//                .build();
//    }
//
//    @Nested
//    @DisplayName("existsByEmail")
//    class ExistsByEmail {
//
//        @Test
//        @DisplayName("deve retornar true quando e-mail já está cadastrado")
//        void deveRetornarTrueQuandoEmailExiste() {
//            fornecedorRepository.save(umFornecedor());
//
//            boolean existe = fornecedorRepository.existsByEmail("teste@email.com");
//
//            assertThat(existe).isTrue();
//        }
//
//        @Test
//        @DisplayName("deve retornar false quando e-mail não está cadastrado")
//        void deveRetornarFalseQuandoEmailNaoExiste() {
//            boolean existe = fornecedorRepository.existsByEmail("naoexiste@email.com");
//
//            assertThat(existe).isFalse();
//        }
//
//        @Test
//        @DisplayName("deve ser case-sensitive na busca por e-mail")
//        void deveSerCaseSensitiveNaBuscaPorEmail() {
//            fornecedorRepository.save(umFornecedor());
//
//            boolean existe = fornecedorRepository.existsByEmail("TESTE@EMAIL.COM");
//
//            assertThat(existe).isFalse();
//        }
//    }
//
//    @Nested
//    @DisplayName("save")
//    class Save {
//
//        @Test
//        @DisplayName("deve persistir fornecedor e gerar id automaticamente")
//        void devePersistirFornecedorEGerarId() {
//            Fornecedor salvo = fornecedorRepository.save(umFornecedor());
//
//            assertThat(salvo.getId()).isNotNull();
//            assertThat(salvo.getNome()).isEqualTo("Fornecedor Teste");
//        }
//
//        @Test
//        @DisplayName("deve persistir fornecedor com ativo true")
//        void devePersistirFornecedorComAtivoTrue() {
//            Fornecedor salvo = fornecedorRepository.save(umFornecedor());
//
//            assertThat(salvo.getAtivo()).isTrue();
//        }
//    }
//
//    @Nested
//    @DisplayName("findById")
//    class FindById {
//
//        @Test
//        @DisplayName("deve retornar fornecedor quando id existe")
//        void deveRetornarFornecedorQuandoIdExiste() {
//            Fornecedor salvo = fornecedorRepository.save(umFornecedor());
//
//            Optional<Fornecedor> encontrado = fornecedorRepository.findById(salvo.getId());
//
//            assertThat(encontrado).isPresent();
//            assertThat(encontrado.get().getEmail()).isEqualTo("teste@email.com");
//        }
//
//        @Test
//        @DisplayName("deve retornar Optional vazio quando id não existe")
//        void deveRetornarVazioQuandoIdNaoExiste() {
//            Optional<Fornecedor> encontrado = fornecedorRepository.findById(999L);
//
//            assertThat(encontrado).isEmpty();
//        }
//    }
//}
