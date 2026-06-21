package com.aratechmoveis.almoxarifado.fornecedor.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record Representante(
        String nomeRepresentante,
        String telefoneRepresentante,
        String emailRepresentante
) {
}
