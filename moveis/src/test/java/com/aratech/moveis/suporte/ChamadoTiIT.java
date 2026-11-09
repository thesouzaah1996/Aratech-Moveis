package com.aratech.moveis.suporte;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.suporte.chamados.dto.ChamadoTiDTO;
import com.aratech.moveis.suporte.chamados.enums.StatusChamadoTi;
import com.aratech.moveis.suporte.chamados.exception.ChamadoTiException;
import com.aratech.moveis.suporte.chamados.model.ChamadoTi;
import com.aratech.moveis.suporte.chamados.repository.ChamadoTiRepository;
import com.aratech.moveis.suporte.chamados.service.ChamadoTiService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@Transactional
class ChamadoTiIT {
    @Autowired ChamadoTiService service;
    @Autowired ChamadoTiRepository repository;

    ChamadoTiDTO dados(String titulo, String prioridade, UUID chave) {
        return new ChamadoTiDTO(
                "Hardware",
                prioridade,
                "",
                titulo,
                "Ana Silva",
                "Compras",
                "1234",
                "Monitor sem imagem\nCabo verificado",
                "",
                chave);
    }

    @Test
    void numeroPersistenciaEReenvioSemDuplicacao() {
        var dto = dados("IT-" + UUID.randomUUID(), "Alta", UUID.randomUUID());
        var a = service.abrir(dto).getChamado();
        assertTrue(a.numero().matches("TI-[0-9]{4}-[0-9]{4,}"));
        assertEquals("Aberto", a.status());
        assertEquals("1234", a.contato());
        assertEquals(a, service.buscar(a.id()).getChamado());
        assertEquals(a, service.abrir(dto).getChamado());
        var entidade = repository.findById(a.id()).orElseThrow();
        entidade.setStatus(StatusChamadoTi.CONCLUIDO);
        repository.saveAndFlush(entidade);
        assertEquals("Conclu\u00eddo", service.abrir(dto).getChamado().status());
        assertThrows(
                ChamadoTiException.class,
                () -> service.abrir(dados("Outro titulo", "Alta", dto.chaveOperacao())));
        assertEquals(a.id(), service.listar(0, 8, null, a.numero()).getChamados().getFirst().id());
    }

    @Test
    void filtrosPaginacaoEPrioridadeSaoGlobais() {
        String marca = "IT-" + UUID.randomUUID();
        var baixa = service.abrir(dados(marca + " baixa", "Baixa", UUID.randomUUID())).getChamado();
        var critica =
                service.abrir(dados(marca + " critica", "Cr\u00edtica", UUID.randomUUID()))
                        .getChamado();
        assertNotEquals(baixa.numero(), critica.numero());
        var pagina = service.listar(0, 1, "Aberto", marca);
        assertEquals(2L, pagina.getTotal());
        assertEquals(critica.id(), pagina.getChamados().getFirst().id());
        assertEquals(baixa.id(), service.listar(1, 1, null, marca).getChamados().getFirst().id());
        assertEquals(0L, service.listar(0, 8, "Cancelado", marca).getTotal());
        assertEquals(0L, service.listar(0, 8, null, marca + "%").getTotal());
        assertEquals(0L, service.listar(0, 8, null, marca + "_").getTotal());
    }

    @Test
    void anoDoNumeroUsaHorarioBrasileiro() {
        var c =
                ChamadoTi.builder()
                        .id(42L)
                        .dataAbertura(Instant.parse("2026-01-01T01:00:00Z"))
                        .build();
        c.definirNumero();
        assertEquals("TI-2025-0042", c.getNumero());
    }
}
