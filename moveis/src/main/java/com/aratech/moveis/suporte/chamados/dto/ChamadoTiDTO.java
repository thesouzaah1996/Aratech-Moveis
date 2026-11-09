package com.aratech.moveis.suporte.chamados.dto;

import java.util.UUID;

public record ChamadoTiDTO(
        String tipo,
        String prioridade,
        String moduloAfetado,
        String titulo,
        String solicitante,
        String setor,
        String contato,
        String descricao,
        String tentativasSolucao,
        UUID chaveOperacao) {}
