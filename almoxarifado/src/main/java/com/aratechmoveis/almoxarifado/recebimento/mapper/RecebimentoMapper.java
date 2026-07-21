package com.aratechmoveis.almoxarifado.recebimento.mapper;

import com.aratechmoveis.almoxarifado.recebimento.dto.RecebimentoDTO;
import com.aratechmoveis.almoxarifado.recebimento.entity.Recebimento;
import com.aratechmoveis.almoxarifado.recebimento.subscriber.representation.RecebimentoRepresentation;
import org.springframework.stereotype.Component;

@Component
public class RecebimentoMapper {

    public Recebimento map(RecebimentoRepresentation recebimentoRepresentation) {
        Recebimento novoRecebimento = new Recebimento();
        novoRecebimento.setNotaFiscal(recebimentoRepresentation.notaFiscal());
        novoRecebimento.setEmpresa(recebimentoRepresentation.empresa());
        novoRecebimento.setNomeMotorista(recebimentoRepresentation.nomeMotorista());
        novoRecebimento.setPlaca(recebimentoRepresentation.placa());
        novoRecebimento.setDescricaoCarga(recebimentoRepresentation.descricaoCarga());
        novoRecebimento.setSetorResponsavel(recebimentoRepresentation.setorResponsavel());

        return novoRecebimento;
    }
}
