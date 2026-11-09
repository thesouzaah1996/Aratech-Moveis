package com.aratech.moveis.manutencao.chamados.dto;

import java.util.UUID;

public record ChamadoDTO(
        String equipamento,
        String tipo,
        String prioridade,
        String solicitante,
        String setor,
        String telefone,
        String descricao,
        UUID chaveOperacao) {}
