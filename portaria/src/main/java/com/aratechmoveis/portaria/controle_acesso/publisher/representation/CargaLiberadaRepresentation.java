package com.aratechmoveis.portaria.controle_acesso.publisher.representation;

import com.aratechmoveis.portaria.controle_acesso.entity.StatusCaminhao;

public record CargaLiberadaRepresentation(
        String notaFiscal,
        StatusCaminhao status
) {
}
