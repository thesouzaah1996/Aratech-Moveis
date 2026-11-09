package com.aratech.moveis.manutencao.pecas.dto;

import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.pecas.enums.FinalidadePeca;

import java.math.BigDecimal;
import java.time.Instant;

public record SolicitacaoPecaResumoDTO(
        Long id,
        String nomePeca,
        String codigo,
        BigDecimal quantidade,
        String unidade,
        String equipamento,
        FinalidadePeca finalidade,
        PrioridadeChamado prioridade,
        String solicitante,
        String setor,
        String telefone,
        String observacoes,
        Instant criadoEm,
        StatusChamado status) {
    public static SolicitacaoPecaResumoDTO from(Chamado c) {
        return new SolicitacaoPecaResumoDTO(
                c.getId(),
                c.getNomePeca(),
                c.getCodigoPeca(),
                c.getQuantidade(),
                c.getUnidade(),
                c.getEquipamento(),
                c.getFinalidade(),
                c.getPrioridade(),
                c.getSolicitante(),
                c.getSetor(),
                c.getTelefone(),
                c.getDescricao(),
                c.getDataAbertura(),
                c.getStatus());
    }
}
