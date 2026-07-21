package com.aratechmoveis.almoxarifado.fornecedor.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public record Representante(
        @Size(max = 150, message = "O nome do representante deve ter no máximo 150 caracteres.")
        String nomeRepresentante,

        @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$", message = "Informe um telefone válido para o representante.")
        String telefoneRepresentante,

        @Email(message = "O e-mail do representante informado não tem um formato válido.")
        @Size(max = 150, message = "O e-mail do representante não pode ultrapassar 150 caracteres.")
        String emailRepresentante
) {
}
