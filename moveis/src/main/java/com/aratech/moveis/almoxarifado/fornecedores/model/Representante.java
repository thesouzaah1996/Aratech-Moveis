package com.aratech.moveis.almoxarifado.fornecedores.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;

import lombok.*;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Representante {
    @NotBlank
    @Size(min = 2, max = 150)
    @Column(name = "nome_representante", nullable = false, length = 150)
    private String nomeRepresentante;

    @NotBlank
    @Pattern(regexp = "\\d{10,11}")
    @Column(name = "telefone_representante", nullable = false, length = 11)
    private String telefoneRepresentante;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "email_representante", nullable = false, length = 150)
    private String emailRepresentante;
}
