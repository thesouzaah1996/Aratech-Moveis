package com.aratech.moveis.rh.perfis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aratech.moveis.rh.perfis.dto.PerfilDTO;
import com.aratech.moveis.rh.perfis.exception.PerfilException;
import com.aratech.moveis.rh.perfis.model.Perfil;
import com.aratech.moveis.rh.perfis.repository.PerfilRepository;
import com.aratech.moveis.rh.perfis.service.imp.PerfilServiceImp;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.util.*;

class PerfilServiceImpTest {
    private final PerfilRepository repository = mock(PerfilRepository.class);
    private final PerfilServiceImp service = new PerfilServiceImp(repository);

    @Test
    void criaPerfilSomenteComNomeNormalizado() {
        when(repository.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            Perfil p = i.getArgument(0);
                            p.setId(1L);
                            return p;
                        });
        var salvo = service.criar(new PerfilDTO("  Operador  de estoque  "));
        assertEquals("OPERADOR DE ESTOQUE", salvo.nome());
        assertEquals(1L, salvo.id());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {"   ", "AB", "<script>", "Nome\nperfil", "Nome\tperfil", "Nome\u200Bperfil"})
    void rejeitaNomeInvalido(String nome) {
        assertEquals(
                HttpStatus.BAD_REQUEST,
                assertThrows(PerfilException.class, () -> service.criar(new PerfilDTO(nome)))
                        .getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaNomeLongoEDadosAusentes() {
        assertThrows(PerfilException.class, () -> service.criar(new PerfilDTO("A".repeat(101))));
        assertThrows(PerfilException.class, () -> service.criar(null));
        verifyNoInteractions(repository);
    }

    @Test
    void impedeDuplicidadeSemDistinguirMaiusculas() {
        when(repository.existsByNomeIgnoreCase("OPERADOR")).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(PerfilException.class, () -> service.criar(new PerfilDTO("operador")))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void atualizaSomenteNome() {
        Perfil perfil = Perfil.builder().id(1L).nome("OPERADOR").build();
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(perfil));
        when(repository.saveAndFlush(any())).thenReturn(perfil);
        assertEquals("SUPERVISOR", service.atualizar(1L, new PerfilDTO("Supervisor")).nome());
        assertEquals(1L, perfil.getId());
    }

    @Test
    void impedeRenomearParaPerfilExistente() {
        when(repository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(Perfil.builder().id(1L).nome("OPERADOR").build()));
        when(repository.existsByNomeIgnoreCaseAndIdNot("SUPERVISOR", 1L)).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(
                                PerfilException.class,
                                () -> service.atualizar(1L, new PerfilDTO("Supervisor")))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejeitaIdInvalidoOuInexistente() {
        assertThrows(
                PerfilException.class, () -> service.atualizar(null, new PerfilDTO("Operador")));
        assertThrows(PerfilException.class, () -> service.atualizar(0L, new PerfilDTO("Operador")));
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(
                                PerfilException.class,
                                () -> service.atualizar(1L, new PerfilDTO("Operador")))
                        .getStatus());
    }

    @Test
    void preservaPaginacaoEBuscaNoServidor() {
        Pageable page = PageRequest.of(1, 8, Sort.by("id"));
        when(repository.findByNomeContainingIgnoreCase("Operador", page))
                .thenReturn(
                        new PageImpl<>(
                                List.of(Perfil.builder().id(9L).nome("OPERADOR").build()),
                                page,
                                17));
        var resultado = service.listar(1, 8, " Operador ");
        assertEquals(17, resultado.getTotalElements());
        assertEquals(3, resultado.getTotalPages());
        assertEquals("OPERADOR", resultado.getContent().getFirst().nome());
    }

    @Test
    void rejeitaPaginacaoEBuscaInvalidas() {
        assertThrows(PerfilException.class, () -> service.listar(-1, 8, ""));
        assertThrows(PerfilException.class, () -> service.listar(0, 101, ""));
        assertThrows(PerfilException.class, () -> service.listar(Integer.MAX_VALUE, 8, ""));
        assertThrows(PerfilException.class, () -> service.listar(0, 8, "A".repeat(101)));
        verifyNoInteractions(repository);
    }
}
