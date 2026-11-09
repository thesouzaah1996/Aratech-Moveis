package com.aratech.moveis.assistencia.estoque.dto;

import com.aratech.moveis.assistencia.estoque.model.PecaAssistencia;

import java.math.BigDecimal;
import java.time.Instant;

public record PecaAssistenciaResumoDTO(
        Long id,
        String nome,
        String codigo,
        String modelo,
        BigDecimal quantidadeMinima,
        BigDecimal quantidade,
        String unidade,
        String localizacao,
        String descricao,
        boolean ativo,
        Instant criadoEm) {
    public static PecaAssistenciaResumoDTO from(PecaAssistencia p) {
        return new PecaAssistenciaResumoDTO(
                p.getId(),
                p.getNome(),
                p.getCodigo(),
                p.getModelo(),
                p.getQuantidadeMinima(),
                p.getQuantidade(),
                p.getUnidade(),
                p.getLocalizacao(),
                p.getDescricao(),
                p.isAtivo(),
                p.getCriadoEm());
    }
}
