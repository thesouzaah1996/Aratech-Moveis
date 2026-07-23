package com.aratechmoveis.almoxarifado.recebimento.subscriber.representation;

import com.aratechmoveis.almoxarifado.recebimento.subscriber.TipoEventoRecebimento;

public record RecebimentoRepresentation(
         TipoEventoRecebimento tipo,
         String notaFiscal,
         String empresa,
         String nomeMotorista,
         String descricaoCarga,
         String placa,
         String setorResponsavel,
         String status
) {}
