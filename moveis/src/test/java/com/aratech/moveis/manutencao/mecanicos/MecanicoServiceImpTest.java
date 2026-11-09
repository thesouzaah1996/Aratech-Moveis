package com.aratech.moveis.manutencao.mecanicos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;
import com.aratech.moveis.manutencao.mecanicos.dto.MecanicoDTO;
import com.aratech.moveis.manutencao.mecanicos.model.Mecanico;
import com.aratech.moveis.manutencao.mecanicos.repository.MecanicoRepository;
import com.aratech.moveis.manutencao.mecanicos.service.imp.MecanicoServiceImp;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.*;

class MecanicoServiceImpTest {
    final MecanicoRepository repo = mock(MecanicoRepository.class);
    final MecanicoServiceImp service = new MecanicoServiceImp(repo, new ChamadoValidator());

    @Test
    void cadastroAtivoNormalizaNomeEDeduplicaEspecialidades() {
        when(repo.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            Mecanico m = i.getArgument(0);
                            m.setId(7L);
                            return m;
                        });
        var r =
                service.salvar(
                        null,
                        new MecanicoDTO(" Ana   Silva ", List.of("PRENSA", "PRENSA"), "MANHA"));
        assertTrue(r.getMecanico().ativo());
        assertEquals("Ana Silva", r.getMecanico().nome());
        assertEquals(List.of("PRENSA"), r.getMecanico().especialidades());
    }

    @Test
    void rejeitaCadastroInvalidoSemPersistir() {
        assertThrows(ChamadoException.class, () -> service.salvar(null, null));
        assertThrows(
                ChamadoException.class,
                () -> service.salvar(null, new MecanicoDTO("Ana Silva", List.of(), "MANHA")));
        assertThrows(
                ChamadoException.class,
                () ->
                        service.salvar(
                                null, new MecanicoDTO("Ana Silva", List.of("OUTRA"), "MANHA")));
        assertThrows(
                ChamadoException.class,
                () ->
                        service.salvar(
                                null, new MecanicoDTO("Ana Silva", List.of("PRENSA"), "OUTRO")));
        verifyNoInteractions(repo);
    }

    @Test
    void filtraAtivosNoServidor() {
        when(repo.findByAtivo(eq(true), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 8)));
        assertTrue(service.listar(0, 8, true).getMecanicos().isEmpty());
        verify(repo).findByAtivo(eq(true), any(Pageable.class));
    }
}
