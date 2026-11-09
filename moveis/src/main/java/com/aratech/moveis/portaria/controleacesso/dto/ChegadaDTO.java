package com.aratech.moveis.portaria.controleacesso.dto;

import java.util.UUID;

public record ChegadaDTO(
        String notaFiscal,
        String empresa,
        String nomeMotorista,
        String placa,
        String descricaoCarga,
        String setorResponsavel,
        UUID chaveOperacao,
        String tipoOperacao) {
    public ChegadaDTO(
            String notaFiscal,
            String empresa,
            String nomeMotorista,
            String placa,
            String descricaoCarga,
            String setorResponsavel,
            UUID chaveOperacao) {
        this(
                notaFiscal,
                empresa,
                nomeMotorista,
                placa,
                descricaoCarga,
                setorResponsavel,
                chaveOperacao,
                null);
    }
}
