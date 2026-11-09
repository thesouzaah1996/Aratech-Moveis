package com.aratech.moveis.assistencia.lotes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aratech.moveis.assistencia.lotes.model.LoteAssistencia;
import com.aratech.moveis.assistencia.lotes.repository.LoteAssistenciaRepository;
import com.aratech.moveis.assistencia.lotes.service.LoteAssistenciaService;
import com.aratech.moveis.assistencia.lotes.service.imp.LoteAssistenciaServiceImp;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

class LoteAssistenciaServiceTest {
    final LoteAssistenciaRepository repo = mock(LoteAssistenciaRepository.class);
    final LoteAssistenciaService service = new LoteAssistenciaServiceImp(repo);

    @Test
    void fluxoPreservaConclusaoDaEmbalagemAoCarregar() {
        var lote = new LoteAssistencia();
        lote.setId(1L);
        lote.setStatus("Não iniciado");
        when(repo.findById(1L)).thenReturn(Optional.of(lote));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        service.avancar(1L, "iniciar");
        assertEquals("Em andamento", lote.getStatus());
        assertNotNull(lote.getIniciadoEm());
        service.avancar(1L, "finalizar");
        var finalizado = lote.getFinalizadoEm();
        assertEquals("Finalizado", lote.getStatus());
        assertNotNull(finalizado);
        service.avancar(1L, "carregar");
        assertNotNull(lote.getCarregadoEm());
        assertEquals(finalizado, lote.getFinalizadoEm());
        assertThrows(ResponseStatusException.class, () -> service.avancar(1L, "carregar"));
        assertThrows(ResponseStatusException.class, () -> service.avancar(1L, "iniciar"));
    }

    @Test
    void impedeFinalizarOuCarregarAntesDaHora() {
        var lote = new LoteAssistencia();
        lote.setStatus("Não iniciado");
        when(repo.findById(1L)).thenReturn(Optional.of(lote));
        assertThrows(ResponseStatusException.class, () -> service.avancar(1L, "finalizar"));
        assertThrows(ResponseStatusException.class, () -> service.avancar(1L, "carregar"));
        verify(repo, never()).save(any());
    }

    @Test
    void reenvioDaAdministracaoNaoDuplicaNemReiniciaLote() {
        var entrada = new LoteAssistencia();
        entrada.setOrigemId(7L);
        entrada.setCodigo("L-7");
        entrada.setProduto("Portas");
        entrada.setQuantidade(4);
        var existente = new LoteAssistencia();
        existente.setOrigemId(7L);
        existente.setCodigo("L-7");
        existente.setProduto("Portas");
        existente.setQuantidade(4);
        existente.setStatus("Finalizado");
        when(repo.findByOrigemId(7L)).thenReturn(Optional.of(existente));
        assertSame(existente, service.receber(entrada));
        verify(repo, never()).save(any());
    }

    @Test
    void reenvioComQuantidadeDiferentePreservaLoteERejeitaConflito() {
        var existente = new LoteAssistencia();
        existente.setOrigemId(7L);
        existente.setCodigo("L-7");
        existente.setProduto("Portas");
        existente.setQuantidade(4);
        existente.setStatus("Finalizado");
        var entrada = new LoteAssistencia();
        entrada.setOrigemId(7L);
        entrada.setCodigo("L-7");
        entrada.setProduto("Portas");
        entrada.setQuantidade(5);
        when(repo.findByOrigemId(7L)).thenReturn(Optional.of(existente));

        var erro = assertThrows(ResponseStatusException.class, () -> service.receber(entrada));
        assertEquals(409, erro.getStatusCode().value());
        assertEquals(4, existente.getQuantidade());
        assertEquals("Finalizado", existente.getStatus());
        verify(repo, never()).save(any());
    }
}
