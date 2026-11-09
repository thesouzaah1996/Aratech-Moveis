package com.aratech.moveis.portaria.controleacesso.dto;

public record FilaResumoDTO(
        long aguardando,
        long autorizados,
        long emRecebimento,
        Long ultimaChegadaId,
        String ultimaEmpresa) {}
