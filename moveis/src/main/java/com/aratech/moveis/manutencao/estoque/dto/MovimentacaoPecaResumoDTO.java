package com.aratech.moveis.manutencao.estoque.dto;

import com.aratech.moveis.manutencao.estoque.enums.TipoMovimentacaoPeca;
import com.aratech.moveis.manutencao.estoque.model.MovimentacaoPeca;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimentacaoPecaResumoDTO(
        Long id,
        TipoMovimentacaoPeca tipo,
        BigDecimal quantidade,
        BigDecimal saldoAnterior,
        BigDecimal saldoPosterior,
        Instant criadoEm,
        String responsavelNome,
        String motivo) {
    public static MovimentacaoPecaResumoDTO from(MovimentacaoPeca m) {
        return new MovimentacaoPecaResumoDTO(
                m.getId(),
                m.getTipo(),
                m.getQuantidade(),
                m.getSaldoAnterior(),
                m.getSaldoPosterior(),
                m.getCriadoEm(),
                m.getResponsavelNome(),
                m.getMotivo());
    }
}
