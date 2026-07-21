package com.aratechmoveis.almoxarifado.recebimento.subscriber.representation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecebimentoRepresentation(
         String notaFiscal,
         String empresa,
         String nomeMotorista,
         String descricaoCarga,
         String placa,
         String setorResponsavel,
         String status
) {
}
