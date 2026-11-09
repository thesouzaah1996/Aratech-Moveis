package com.aratech.moveis.almoxarifado.fornecedores.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FornecedorDTO {
    @NotBlank(message = "O campo nome e obrigatorio")
    @Size(min = 2, max = 150)
    private String nome;

    @NotBlank(message = "O campo email e obrigatorio")
    @Email(message = "Informe um e-mail valido")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "O campo telefone e obrigatorio")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter 10 ou 11 digitos")
    private String telefone;

    @Valid
    @NotNull(message = "Informe os dados do representante")
    private RepresentanteDTO representante;
}
