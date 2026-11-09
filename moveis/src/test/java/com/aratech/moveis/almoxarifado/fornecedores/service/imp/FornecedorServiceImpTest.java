package com.aratech.moveis.almoxarifado.fornecedores.service.imp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.aratech.moveis.almoxarifado.fornecedores.dto.FornecedorDTO;
import com.aratech.moveis.almoxarifado.fornecedores.dto.RepresentanteDTO;
import com.aratech.moveis.almoxarifado.fornecedores.exception.FornecedorException;
import com.aratech.moveis.almoxarifado.fornecedores.model.Fornecedor;
import com.aratech.moveis.almoxarifado.fornecedores.model.Representante;
import com.aratech.moveis.almoxarifado.fornecedores.repository.FornecedorRepository;
import com.aratech.moveis.almoxarifado.fornecedores.validation.FornecedorValidator;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

class FornecedorServiceImpTest {
    private static final ValidatorFactory VALIDATORS = Validation.buildDefaultValidatorFactory();
    private final FornecedorRepository repository = mock(FornecedorRepository.class);
    private final FornecedorServiceImp service =
            new FornecedorServiceImp(
                    repository, new FornecedorValidator(VALIDATORS.getValidator()));

    @AfterAll
    static void fechar() {
        VALIDATORS.close();
    }

    private Fornecedor existente(boolean ativo) {
        return Fornecedor.builder()
                .id(42L)
                .nome("Ferragens")
                .email("contato@example.com")
                .telefone("4330000000")
                .representante(
                        Representante.builder()
                                .nomeRepresentante("Maria Exemplo")
                                .telefoneRepresentante("43990000000")
                                .emailRepresentante("maria@example.com")
                                .build())
                .ativo(ativo)
                .build();
    }

    @Test
    void cadastraFornecedorAtivaComNomeNormalizadoERetorna201() {
        when(repository.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            Fornecedor fornecedor = i.getArgument(0);
                            fornecedor.setId(42L);
                            return fornecedor;
                        });
        var resposta = service.addFornecedor(dto("  Material   Eletrico  "));
        assertEquals(201, resposta.getStatus());
        assertEquals(42L, resposta.getFornecedorId());
        assertEquals("Material Eletrico", resposta.getNome());
        assertTrue(resposta.getAtivo());
        verify(repository).existsByNomeIgnoreCase("Material Eletrico");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "A"})
    void rejeitaNomesInvalidosSemAcessarBanco(String nome) {
        assertThrows(ConstraintViolationException.class, () -> service.addFornecedor(dto(nome)));
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaNomeMuitoLongoECaracteresDeControle() {
        assertThrows(
                ConstraintViolationException.class,
                () -> service.addFornecedor(dto("x".repeat(151))));
        assertEquals(
                HttpStatus.BAD_REQUEST,
                assertThrows(
                                FornecedorException.class,
                                () -> service.addFornecedor(dto("Nome\nInjetado")))
                        .getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaDtoNulo() {
        assertEquals(
                HttpStatus.BAD_REQUEST,
                assertThrows(FornecedorException.class, () -> service.addFornecedor(null))
                        .getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaDuplicidadeInclusiveFornecedorInativa() {
        when(repository.existsByNomeIgnoreCase("ferragens")).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(
                                FornecedorException.class,
                                () -> service.addFornecedor(dto("ferragens")))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void validaIdsAntesDeQualquerConsultaOuAlteracao() {
        for (Long id : new Long[] {null, 0L, -1L}) {
            assertThrows(FornecedorException.class, () -> service.getFornecedorById(id));
            assertThrows(
                    FornecedorException.class,
                    () -> service.updateFornecedor(dto("Ferragens"), id));
            assertThrows(FornecedorException.class, () -> service.enableFornecedor(id));
            assertThrows(FornecedorException.class, () -> service.disableFornecedor(id));
        }
        verifyNoInteractions(repository);
    }

    @Test
    void inexistenteRetorna404EmTodasOperacoesPorId() {
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(FornecedorException.class, () -> service.getFornecedorById(42L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(
                                FornecedorException.class,
                                () -> service.updateFornecedor(dto("Ferragens"), 42L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(FornecedorException.class, () -> service.enableFornecedor(42L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(FornecedorException.class, () -> service.disableFornecedor(42L))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void consultaFornecedorSemGravar() {
        when(repository.findById(42L)).thenReturn(Optional.of(existente(true)));
        var resposta = service.getFornecedorById(42L);
        assertEquals("Ferragens", resposta.getFornecedor().nome());
        assertEquals(200, resposta.getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void listaPaginadaOrdenaPorIdERetornaTotal() {
        var page = PageRequest.of(0, 20, Sort.by("id").ascending());
        when(repository.buscar("", null, page))
                .thenReturn(new PageImpl<>(List.of(existente(true)), page, 1));
        var resposta = service.getAllFornecedores();
        assertEquals(1L, resposta.getTotal());
        assertEquals(0, resposta.getPagina());
        assertEquals(20, resposta.getTamanho());
        assertEquals(42L, resposta.getFornecedores().getFirst().id());
    }

    @Test
    void listaVaziaNaoRetornaNull() {
        when(repository.buscar(eq(""), isNull(), any(Pageable.class))).thenReturn(Page.empty());
        assertTrue(service.getAllFornecedores().getFornecedores().isEmpty());
    }

    @Test
    void rejeitaPaginacaoInvalidaOuExcessiva() {
        for (int[] p : new int[][] {{-1, 20}, {0, 0}, {0, 101}, {Integer.MAX_VALUE, 100}}) {
            assertThrows(FornecedorException.class, () -> service.getAllFornecedores(p[0], p[1]));
        }
        verifyNoInteractions(repository);
    }

    @Test
    void atualizaSomenteNomePreservandoIdEStatusInativo() {
        var fornecedor = existente(false);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(fornecedor));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var resposta = service.updateFornecedor(dto("  Ferragens Novas  "), 42L);
        assertEquals("Ferragens Novas", resposta.getNome());
        assertEquals(42L, resposta.getFornecedorId());
        assertFalse(resposta.getAtivo());
        verify(repository).existsByNomeIgnoreCaseAndIdNot("Ferragens Novas", 42L);
    }

    @Test
    void atualizacaoNaoPermiteNomeDeOutraFornecedor() {
        var fornecedor = existente(true);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(fornecedor));
        when(repository.existsByNomeIgnoreCaseAndIdNot("Madeiras", 42L)).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(
                                FornecedorException.class,
                                () -> service.updateFornecedor(dto("Madeiras"), 42L))
                        .getStatus());
        assertEquals("Ferragens", fornecedor.getNome());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void atualizacaoValidaDadosMesmoChamadaSemController() {
        assertThrows(
                ConstraintViolationException.class, () -> service.updateFornecedor(dto(" "), 42L));
        assertThrows(FornecedorException.class, () -> service.updateFornecedor(null, 42L));
        verifyNoInteractions(repository);
    }

    @Test
    void ativaEDesativaSemAlterarNome() {
        var fornecedor = existente(true);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(fornecedor));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        assertFalse(service.disableFornecedor(42L).getAtivo());
        assertTrue(service.enableFornecedor(42L).getAtivo());
        assertEquals("Ferragens", fornecedor.getNome());
        verify(repository, times(2)).saveAndFlush(fornecedor);
    }

    @Test
    void repeticaoDeStatusEIdempotente() {
        var fornecedor = existente(true);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(fornecedor));
        assertTrue(service.enableFornecedor(42L).getAtivo());
        fornecedor.setAtivo(false);
        assertFalse(service.disableFornecedor(42L).getAtivo());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void statusNuloRetornaConflito() {
        var fornecedor = existente(true);
        fornecedor.setAtivo(null);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(fornecedor));
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(FornecedorException.class, () -> service.enableFornecedor(42L))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void buscaNomeNoBancoComPaginaEContagemFiltradas() {
        var page = PageRequest.of(2, 8, Sort.by("id").ascending());
        when(repository.buscar("Ferr", null, page))
                .thenReturn(new PageImpl<>(List.of(existente(true)), page, 17));
        var resposta = service.getAllFornecedores(2, 8, " Ferr ", null);
        assertEquals(17L, resposta.getTotal());
        assertEquals(2, resposta.getPagina());
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void lookupFiltraAtivasAntesDePaginar() {
        var page = PageRequest.of(0, 100, Sort.by("id").ascending());
        when(repository.buscar("", true, page))
                .thenReturn(new PageImpl<>(List.of(existente(true)), page, 1));
        assertEquals(1L, service.getAllFornecedores(0, 100, null, true).getTotal());
    }

    @Test
    void rejeitaBuscaExcessivaSemConsultarBanco() {
        assertThrows(
                FornecedorException.class,
                () -> service.getAllFornecedores(0, 8, "a".repeat(151), null));
        verifyNoInteractions(repository);
    }

    private FornecedorDTO dto(String nome) {
        return FornecedorDTO.builder()
                .nome(nome)
                .email("CONTATO@EXAMPLE.COM")
                .telefone("4330000000")
                .representante(
                        new RepresentanteDTO("Maria Exemplo", "43990000000", "MARIA@EXAMPLE.COM"))
                .build();
    }

    @Test
    void exigeRepresentanteEValidaTodosSeusDados() {
        var dados = dto("Madeiras");
        dados.setRepresentante(null);
        assertThrows(ConstraintViolationException.class, () -> service.addFornecedor(dados));
        dados.setRepresentante(new RepresentanteDTO(" ", "abc", "invalido"));
        var erro =
                assertThrows(
                        ConstraintViolationException.class, () -> service.addFornecedor(dados));
        assertTrue(
                erro.getConstraintViolations().stream()
                        .anyMatch(
                                v ->
                                        v.getPropertyPath()
                                                .toString()
                                                .equals("representante.emailRepresentante")));
        assertTrue(
                erro.getConstraintViolations().stream()
                        .anyMatch(
                                v ->
                                        v.getPropertyPath()
                                                .toString()
                                                .equals("representante.telefoneRepresentante")));
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaEmailTelefoneEDadosObrigatoriosInvalidosDoFornecedor() {
        var dados = dto("Madeiras");
        dados.setEmail("sem-arroba");
        dados.setTelefone("123");
        var erro =
                assertThrows(
                        ConstraintViolationException.class, () -> service.addFornecedor(dados));
        assertTrue(
                erro.getConstraintViolations().stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(
                erro.getConstraintViolations().stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("telefone")));
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaEmailDuplicadoNoCadastroENaAtualizacao() {
        when(repository.existsByEmailIgnoreCase("contato@example.com")).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(
                                FornecedorException.class,
                                () -> service.addFornecedor(dto("Madeiras")))
                        .getStatus());
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(existente(true)));
        when(repository.existsByEmailIgnoreCaseAndIdNot("contato@example.com", 42L))
                .thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(
                                FornecedorException.class,
                                () -> service.updateFornecedor(dto("Madeiras"), 42L))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void normalizaEmailsEPreservaContatoDoRepresentanteNaResposta() {
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var resultado = service.addFornecedor(dto("Madeiras")).getFornecedor();
        assertEquals("contato@example.com", resultado.email());
        assertEquals("maria@example.com", resultado.representante().getEmailRepresentante());
        assertEquals("43990000000", resultado.representante().getTelefoneRepresentante());
    }

    @Test
    void escapaCuringasDeBuscaParaPesquisaLiteral() {
        var page = PageRequest.of(0, 8, Sort.by("id").ascending());
        when(repository.buscar("!%!_!!", null, page)).thenReturn(Page.empty(page));
        service.getAllFornecedores(0, 8, "%_!", null);
        verify(repository).buscar("!%!_!!", null, page);
    }
}
