package com.aratech.moveis.manutencao.estoque.dto;

import com.aratech.moveis.manutencao.estoque.model.PecaEstoque;

import java.math.BigDecimal;
import java.time.Instant;

public record PecaEstoqueResumoDTO(
        Long id,
        String nome,
        String codigo,
        BigDecimal quantidade,
        String unidade,
        String localizacao,
        String descricao,
        boolean ativo,
        Instant criadoEm,
        BigDecimal quantidadeMinima) {
    public static PecaEstoqueResumoDTO from(PecaEstoque p) {
        return new PecaEstoqueResumoDTO(
                p.getId(),
                p.getNome(),
                p.getCodigo(),
                p.getQuantidade(),
                p.getUnidade(),
                p.getLocalizacao(),
                p.getDescricao(),
                p.isAtivo(),
                p.getCriadoEm(),
                p.getQuantidadeMinima());
    }
}
