package com.aratech.moveis.portaria.controleacesso.dto;

import com.aratech.moveis.portaria.controleacesso.enums.StatusAcesso;
import com.aratech.moveis.portaria.controleacesso.model.RegistroChegada;

import java.time.Instant;

public record ChegadaResumoDTO(
        Long id,
        String notaFiscal,
        String empresa,
        String nomeMotorista,
        String placa,
        String descricaoCarga,
        String setorResponsavel,
        StatusAcesso status,
        Instant dataChegada,
        Instant dataAutorizacao,
        Instant dataEntrada,
        Instant dataFinalizacao,
        String tipoOperacao) {
    public ChegadaResumoDTO(
            Long id,
            String notaFiscal,
            String empresa,
            String nomeMotorista,
            String placa,
            String descricaoCarga,
            String setorResponsavel,
            StatusAcesso status,
            Instant dataChegada,
            Instant dataAutorizacao,
            Instant dataEntrada,
            Instant dataFinalizacao) {
        this(
                id,
                notaFiscal,
                empresa,
                nomeMotorista,
                placa,
                descricaoCarga,
                setorResponsavel,
                status,
                dataChegada,
                dataAutorizacao,
                dataEntrada,
                dataFinalizacao,
                null);
    }

    public static ChegadaResumoDTO from(RegistroChegada r) {
        return new ChegadaResumoDTO(
                r.getId(),
                r.getNotaFiscal(),
                r.getEmpresa(),
                r.getNomeMotorista(),
                r.getPlaca(),
                r.getDescricaoCarga(),
                r.getSetorResponsavel(),
                r.getStatus(),
                r.getDataChegada(),
                r.getDataAutorizacao(),
                r.getDataEntrada(),
                r.getDataFinalizacao(),
                r.getTipoOperacao());
    }
}
