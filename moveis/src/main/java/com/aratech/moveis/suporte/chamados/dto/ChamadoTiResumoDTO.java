package com.aratech.moveis.suporte.chamados.dto;

import com.aratech.moveis.suporte.chamados.model.ChamadoTi;

import java.time.Instant;

public record ChamadoTiResumoDTO(
        Long id,
        String numero,
        String tipo,
        String prioridade,
        String moduloAfetado,
        String titulo,
        String solicitante,
        String setor,
        String contato,
        String descricao,
        String tentativasSolucao,
        String status,
        Instant dataAbertura) {
    public static ChamadoTiResumoDTO from(ChamadoTi c) {
        return new ChamadoTiResumoDTO(
                c.getId(),
                c.getNumero(),
                c.getTipo().toString(),
                c.getPrioridade().toString(),
                c.getModuloAfetado(),
                c.getTitulo(),
                c.getSolicitante(),
                c.getSetor(),
                c.getContato(),
                c.getDescricao(),
                c.getTentativasSolucao(),
                c.getStatus().toString(),
                c.getDataAbertura());
    }
}
