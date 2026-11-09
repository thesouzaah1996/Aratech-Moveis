package com.aratech.moveis.assistencia.estoque.dto;

import com.aratech.moveis.assistencia.estoque.enums.TipoMovimentoAssistencia;
import com.aratech.moveis.assistencia.estoque.model.MovimentoAssistencia;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimentoAssistenciaResumoDTO(
        Long id,
        TipoMovimentoAssistencia tipo,
        BigDecimal quantidade,
        BigDecimal saldoAnterior,
        BigDecimal saldoPosterior,
        Instant criadoEm,
        String responsavelNome,
        String motivo) {
    public static MovimentoAssistenciaResumoDTO from(MovimentoAssistencia m) {
        return new MovimentoAssistenciaResumoDTO(
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
