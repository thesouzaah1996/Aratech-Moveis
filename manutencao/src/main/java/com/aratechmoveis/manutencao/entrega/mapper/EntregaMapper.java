package com.aratechmoveis.manutencao.entrega.mapper;

import com.aratechmoveis.manutencao.entrega.entity.Entrega;
import com.aratechmoveis.manutencao.entrega.subscriber.representation.EntregaRepresentation;
import org.springframework.stereotype.Component;

@Component
public class EntregaMapper {

    public Entrega map(EntregaRepresentation recebimentoRepresentation) {
        Entrega novoRecebimento = new Entrega();
        novoRecebimento.setNotaFiscal(recebimentoRepresentation.notaFiscal());
        novoRecebimento.setEmpresa(recebimentoRepresentation.empresa());
        novoRecebimento.setNomeMotorista(recebimentoRepresentation.nomeMotorista());
        novoRecebimento.setPlaca(recebimentoRepresentation.placa());
        novoRecebimento.setDescricaoCarga(recebimentoRepresentation.descricaoCarga());
        novoRecebimento.setSetorResponsavel(recebimentoRepresentation.setorResponsavel());

        return novoRecebimento;
    }
}

