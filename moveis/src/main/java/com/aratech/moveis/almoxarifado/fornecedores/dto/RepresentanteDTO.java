package com.aratech.moveis.almoxarifado.fornecedores.dto;

import jakarta.validation.constraints.*;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepresentanteDTO {
    @NotBlank(message = "O campo nomeRepresentante e obrigatorio")
    @Size(min = 2, max = 150)
    private String nomeRepresentante;

    @NotBlank(message = "O campo telefoneRepresentante e obrigatorio")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter 10 ou 11 digitos")
    private String telefoneRepresentante;

    @NotBlank(message = "O campo emailRepresentante e obrigatorio")
    @Email(message = "Informe um e-mail valido")
    @Size(max = 150)
    private String emailRepresentante;
}
