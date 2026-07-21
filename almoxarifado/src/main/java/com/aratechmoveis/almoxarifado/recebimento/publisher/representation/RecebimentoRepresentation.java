package com.aratechmoveis.almoxarifado.recebimento.publisher.representation;

import com.aratechmoveis.almoxarifado.recebimento.entity.StatusRecebimento;

public record RecebimentoRepresentation(
        String notaFiscal,
        StatusRecebimento status
) {
}
