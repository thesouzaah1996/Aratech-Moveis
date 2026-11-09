package com.aratech.moveis.manutencao.estoque.dto;

import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PecaEstoqueDTO {
    @NotBlank(message = "Informe o nome da peca")
    @Size(min = 2, max = 150)
    private String nome;

    @NotBlank(message = "Informe o codigo")
    @Size(max = 100)
    private String codigo;

    @NotBlank
    @Pattern(regexp = "un|kg|L|m|cx|par", message = "Selecione uma unidade valida")
    private String unidade;

    @NotBlank(message = "Informe a localizacao")
    @Size(min = 2, max = 100)
    private String localizacao;

    @Size(max = 2000)
    private String descricao;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("999999999999.999")
    @Digits(integer = 12, fraction = 3)
    private BigDecimal quantidade;
}
