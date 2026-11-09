package com.aratech.moveis.manutencao.pecas.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SolicitacaoPecaDTO(
        String nomePeca,
        String codigo,
        BigDecimal quantidade,
        String unidade,
        String equipamento,
        String finalidade,
        String prioridade,
        String solicitante,
        String setor,
        String telefone,
        String observacoes,
        UUID chaveOperacao) {}
