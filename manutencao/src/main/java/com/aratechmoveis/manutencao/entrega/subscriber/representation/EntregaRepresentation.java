package com.aratechmoveis.manutencao.entrega.subscriber.representation;

import com.aratechmoveis.manutencao.entrega.subscriber.representation.subscriber.TipoEventoEntrega;

public record EntregaRepresentation(
        TipoEventoEntrega tipo,
        String notaFiscal,
        String empresa,
        String nomeMotorista,
        String descricaoCarga,
        String placa,
        String setorResponsavel,
        String status
) {}
