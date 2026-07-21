package com.aratechmoveis.portaria.controle_acesso.publisher.representation;


import com.aratechmoveis.portaria.controle_acesso.entity.SetorResponsavel;
import jakarta.persistence.*;

public record RegistroChegadaRepresentation (
         String notaFiscal,
         String empresa,
         String nomeMotorista,
         String placa,
         String descricaoCarga,
         SetorResponsavel setorResponsavel
) {
}
