package com.aratech.moveis.almoxarifado.produtos.dto;

import com.aratech.moveis.almoxarifado.produtos.enums.TipoMovimentacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record MovimentacaoResumoDTO(
        Long id,
        TipoMovimentacao tipo,
        Integer quantidade,
        Integer saldoAnterior,
        Integer saldoPosterior,
        UUID chaveOperacao,
        LocalDateTime criadoEm,
        Long responsavelId,
        String responsavelNome,
        String motivo) {}
