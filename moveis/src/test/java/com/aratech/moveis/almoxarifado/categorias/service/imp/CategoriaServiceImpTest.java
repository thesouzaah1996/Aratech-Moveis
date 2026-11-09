package com.aratech.moveis.almoxarifado.categorias.service.imp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.aratech.moveis.almoxarifado.categorias.dto.CategoriaDTO;
import com.aratech.moveis.almoxarifado.categorias.exception.CategoriaException;
import com.aratech.moveis.almoxarifado.categorias.model.Categoria;
import com.aratech.moveis.almoxarifado.categorias.repository.CategoriaRepository;
import com.aratech.moveis.almoxarifado.categorias.validation.CategoriaValidator;

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

class CategoriaServiceImpTest {
    private static final ValidatorFactory VALIDATORS = Validation.buildDefaultValidatorFactory();
    private final CategoriaRepository repository = mock(CategoriaRepository.class);
    private final CategoriaServiceImp service =
            new CategoriaServiceImp(repository, new CategoriaValidator(VALIDATORS.getValidator()));

    @AfterAll
    static void fechar() {
        VALIDATORS.close();
    }

    private Categoria existente(boolean ativo) {
        return Categoria.builder().id(42L).nome("Ferragens").ativo(ativo).build();
    }

    @Test
    void cadastraCategoriaAtivaComNomeNormalizadoERetorna201() {
        when(repository.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            Categoria categoria = i.getArgument(0);
                            categoria.setId(42L);
                            return categoria;
                        });
        var resposta =
                service.addCategoria(
                        CategoriaDTO.builder().nome("  Material   Eletrico  ").build());
        assertEquals(201, resposta.getStatus());
        assertEquals(42L, resposta.getCategoriaId());
        assertEquals("Material Eletrico", resposta.getNome());
        assertTrue(resposta.getAtivo());
        verify(repository).existsByNomeIgnoreCase("Material Eletrico");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "A"})
    void rejeitaNomesInvalidosSemAcessarBanco(String nome) {
        assertThrows(
                ConstraintViolationException.class,
                () -> service.addCategoria(CategoriaDTO.builder().nome(nome).build()));
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaNomeMuitoLongoECaracteresDeControle() {
        assertThrows(
                ConstraintViolationException.class,
                () -> service.addCategoria(CategoriaDTO.builder().nome("x".repeat(101)).build()));
        assertEquals(
                HttpStatus.BAD_REQUEST,
                assertThrows(
                                CategoriaException.class,
                                () ->
                                        service.addCategoria(
                                                CategoriaDTO.builder()
                                                        .nome("Nome\nInjetado")
                                                        .build()))
                        .getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaDtoNulo() {
        assertEquals(
                HttpStatus.BAD_REQUEST,
                assertThrows(CategoriaException.class, () -> service.addCategoria(null))
                        .getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaDuplicidadeInclusiveCategoriaInativa() {
        when(repository.existsByNomeIgnoreCase("ferragens")).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(
                                CategoriaException.class,
                                () ->
                                        service.addCategoria(
                                                CategoriaDTO.builder().nome("ferragens").build()))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void validaIdsAntesDeQualquerConsultaOuAlteracao() {
        for (Long id : new Long[] {null, 0L, -1L}) {
            assertThrows(CategoriaException.class, () -> service.getCategoriaById(id));
            assertThrows(
                    CategoriaException.class,
                    () -> service.updateCategoria(new CategoriaDTO("Ferragens"), id));
            assertThrows(CategoriaException.class, () -> service.enableCategoria(id));
            assertThrows(CategoriaException.class, () -> service.disableCategoria(id));
        }
        verifyNoInteractions(repository);
    }

    @Test
    void inexistenteRetorna404EmTodasOperacoesPorId() {
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(CategoriaException.class, () -> service.getCategoriaById(42L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(
                                CategoriaException.class,
                                () -> service.updateCategoria(new CategoriaDTO("Ferragens"), 42L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(CategoriaException.class, () -> service.enableCategoria(42L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(CategoriaException.class, () -> service.disableCategoria(42L))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void consultaCategoriaSemGravar() {
        when(repository.findById(42L)).thenReturn(Optional.of(existente(true)));
        var resposta = service.getCategoriaById(42L);
        assertEquals("Ferragens", resposta.getCategoria().nome());
        assertEquals(200, resposta.getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void listaPaginadaOrdenaPorIdERetornaTotal() {
        var page = PageRequest.of(0, 20, Sort.by("id").ascending());
        when(repository.findAll(page))
                .thenReturn(new PageImpl<>(List.of(existente(true)), page, 1));
        var resposta = service.getAllCategorias();
        assertEquals(1L, resposta.getTotal());
        assertEquals(0, resposta.getPagina());
        assertEquals(20, resposta.getTamanho());
        assertEquals(42L, resposta.getCategorias().getFirst().id());
    }

    @Test
    void listaVaziaNaoRetornaNull() {
        when(repository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        assertTrue(service.getAllCategorias().getCategorias().isEmpty());
    }

    @Test
    void rejeitaPaginacaoInvalidaOuExcessiva() {
        for (int[] p : new int[][] {{-1, 20}, {0, 0}, {0, 101}, {Integer.MAX_VALUE, 100}}) {
            assertThrows(CategoriaException.class, () -> service.getAllCategorias(p[0], p[1]));
        }
        verifyNoInteractions(repository);
    }

    @Test
    void atualizaSomenteNomePreservandoIdEStatusInativo() {
        var categoria = existente(false);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(categoria));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var resposta = service.updateCategoria(new CategoriaDTO("  Ferragens Novas  "), 42L);
        assertEquals("Ferragens Novas", resposta.getNome());
        assertEquals(42L, resposta.getCategoriaId());
        assertFalse(resposta.getAtivo());
        verify(repository).existsByNomeIgnoreCaseAndIdNot("Ferragens Novas", 42L);
    }

    @Test
    void atualizacaoNaoPermiteNomeDeOutraCategoria() {
        var categoria = existente(true);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(categoria));
        when(repository.existsByNomeIgnoreCaseAndIdNot("Madeiras", 42L)).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(
                                CategoriaException.class,
                                () -> service.updateCategoria(new CategoriaDTO("Madeiras"), 42L))
                        .getStatus());
        assertEquals("Ferragens", categoria.getNome());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void atualizacaoValidaDadosMesmoChamadaSemController() {
        assertThrows(
                ConstraintViolationException.class,
                () -> service.updateCategoria(new CategoriaDTO(" "), 42L));
        assertThrows(CategoriaException.class, () -> service.updateCategoria(null, 42L));
        verifyNoInteractions(repository);
    }

    @Test
    void ativaEDesativaSemAlterarNome() {
        var categoria = existente(true);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(categoria));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        assertFalse(service.disableCategoria(42L).getAtivo());
        assertTrue(service.enableCategoria(42L).getAtivo());
        assertEquals("Ferragens", categoria.getNome());
        verify(repository, times(2)).saveAndFlush(categoria);
    }

    @Test
    void repeticaoDeStatusEIdempotente() {
        var categoria = existente(true);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(categoria));
        assertTrue(service.enableCategoria(42L).getAtivo());
        categoria.setAtivo(false);
        assertFalse(service.disableCategoria(42L).getAtivo());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void statusNuloRetornaConflito() {
        var categoria = existente(true);
        categoria.setAtivo(null);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(categoria));
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(CategoriaException.class, () -> service.enableCategoria(42L))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void buscaNomeNoBancoComPaginaEContagemFiltradas() {
        var page = PageRequest.of(2, 8, Sort.by("id").ascending());
        when(repository.findByNomeContainingIgnoreCase("Ferr", page))
                .thenReturn(new PageImpl<>(List.of(existente(true)), page, 17));
        var resposta = service.getAllCategorias(2, 8, " Ferr ", null);
        assertEquals(17L, resposta.getTotal());
        assertEquals(2, resposta.getPagina());
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void lookupFiltraAtivasAntesDePaginar() {
        var page = PageRequest.of(0, 100, Sort.by("id").ascending());
        when(repository.findByNomeContainingIgnoreCaseAndAtivo("", true, page))
                .thenReturn(new PageImpl<>(List.of(existente(true)), page, 1));
        assertEquals(1L, service.getAllCategorias(0, 100, null, true).getTotal());
    }

    @Test
    void rejeitaBuscaExcessivaSemConsultarBanco() {
        assertThrows(
                CategoriaException.class,
                () -> service.getAllCategorias(0, 8, "a".repeat(101), null));
        verifyNoInteractions(repository);
    }
}
