package com.aratech.moveis.manutencao.fila;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aratech.moveis.manutencao.chamados.enums.OrigemChamado;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.enums.TipoManutencao;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.chamados.repository.ChamadoRepository;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;
import com.aratech.moveis.manutencao.fila.dto.AtribuicaoDTO;
import com.aratech.moveis.manutencao.fila.service.imp.FilaChamadosServiceImp;
import com.aratech.moveis.manutencao.mecanicos.enums.EspecialidadeMecanico;
import com.aratech.moveis.manutencao.mecanicos.enums.TurnoMecanico;
import com.aratech.moveis.manutencao.mecanicos.model.Mecanico;
import com.aratech.moveis.manutencao.mecanicos.repository.MecanicoRepository;
import com.aratech.moveis.manutencao.pecas.enums.FinalidadePeca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

class FilaChamadosServiceImpTest {
    final ChamadoRepository repo = mock(ChamadoRepository.class);
    final MecanicoRepository mecanicos = mock(MecanicoRepository.class);
    final FilaChamadosServiceImp service =
            new FilaChamadosServiceImp(repo, mecanicos, new ChamadoValidator());

    Chamado chamado(OrigemChamado origem) {
        var c =
                Chamado.builder()
                        .id(42L)
                        .origem(origem)
                        .equipamento("Prensa")
                        .prioridade(PrioridadeChamado.ALTA)
                        .solicitante("Ana Silva")
                        .setor("Producao")
                        .descricao("Motor parado")
                        .dataAbertura(Instant.now().minusSeconds(10))
                        .build();
        if (origem == OrigemChamado.PECA) {
            c.setNomePeca("Rolamento");
            c.setQuantidade(BigDecimal.ONE);
            c.setUnidade("un");
            c.setFinalidade(FinalidadePeca.CORRETIVA);
        } else c.setTipo(TipoManutencao.CORRETIVA);
        when(repo.findByIdForUpdate(42L)).thenReturn(Optional.of(c));
        when(repo.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        return c;
    }

    Mecanico mecanico(boolean ativo) {
        var m =
                Mecanico.builder()
                        .id(7L)
                        .nome("Mecanico Teste")
                        .turno(TurnoMecanico.MANHA)
                        .especialidades(Set.of(EspecialidadeMecanico.PRENSA))
                        .ativo(ativo)
                        .build();
        when(mecanicos.findByIdForUpdate(7L)).thenReturn(Optional.of(m));
        return m;
    }

    @ParameterizedTest
    @EnumSource(OrigemChamado.class)
    void atribuiEConcluiOsDoisTiposSemAlterarHorariosAoRepetir(OrigemChamado origem) {
        var c = chamado(origem);
        mecanico(true);
        var atribuido = service.atribuir(42L, new AtribuicaoDTO(7L)).getItem();
        assertEquals(StatusChamado.EM_MANUTENCAO, atribuido.status());
        assertEquals(7L, atribuido.mecanico().id());
        assertNotNull(atribuido.dataInicio());
        assertEquals(origem, atribuido.origem());
        service.atribuir(42L, new AtribuicaoDTO(7L));
        assertEquals(atribuido.dataInicio(), c.getDataInicio());
        var finalizado = service.concluir(42L).getItem();
        assertEquals(StatusChamado.CONCLUIDA, finalizado.status());
        assertNotNull(finalizado.dataConclusao());
        assertEquals(finalizado.dataConclusao(), service.concluir(42L).getItem().dataConclusao());
        verify(repo, times(2)).saveAndFlush(any());
    }

    @Test
    void naoConcluiAntesDaAtribuicao() {
        chamado(OrigemChamado.MANUTENCAO);
        assertThrows(ChamadoException.class, () -> service.concluir(42L));
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void rejeitaMecanicoInativoEChamadoConcluido() {
        var c = chamado(OrigemChamado.PECA);
        mecanico(false);
        assertThrows(ChamadoException.class, () -> service.atribuir(42L, new AtribuicaoDTO(7L)));
        c.setStatus(StatusChamado.CONCLUIDA);
        mecanico(true);
        assertThrows(ChamadoException.class, () -> service.atribuir(42L, new AtribuicaoDTO(7L)));
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void reatribuicaoPreservaInicio() {
        var c = chamado(OrigemChamado.PECA);
        c.setStatus(StatusChamado.EM_MANUTENCAO);
        c.setDataInicio(Instant.now().minusSeconds(5));
        var inicio = c.getDataInicio();
        mecanico(true);
        assertEquals(inicio, service.atribuir(42L, new AtribuicaoDTO(7L)).getItem().dataInicio());
    }

    @Test
    void validaIdsESelecao() {
        assertThrows(ChamadoException.class, () -> service.atribuir(42L, null));
        assertThrows(ChamadoException.class, () -> service.atribuir(42L, new AtribuicaoDTO(0L)));
        assertThrows(ChamadoException.class, () -> service.buscar(0L));
        assertThrows(ChamadoException.class, () -> service.concluir(99L));
    }

    @Test
    void paginaUsaTotalDoBancoSemSepararOrigens() {
        var a = chamado(OrigemChamado.MANUTENCAO);
        var b = chamado(OrigemChamado.PECA);
        b.setId(43L);
        when(repo.findAll(
                        org.mockito.ArgumentMatchers.<Specification<Chamado>>any(),
                        eq(PageRequest.of(1, 8))))
                .thenReturn(new PageImpl<>(List.of(a, b), PageRequest.of(1, 8), 17));
        var p = service.listar(1, 8, "ABERTA", "Prensa");
        assertEquals(17L, p.getTotal());
        assertEquals(2, p.getItens().size());
        assertNotNull(p.getItens().get(1).peca());
        assertThrows(ChamadoException.class, () -> service.listar(0, 101, null, null));
        assertThrows(ChamadoException.class, () -> service.listar(0, 8, "OUTRO", null));
    }
}
