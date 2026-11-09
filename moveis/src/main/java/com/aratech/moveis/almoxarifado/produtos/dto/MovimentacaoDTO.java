package com.aratech.moveis.almoxarifado.produtos.dto;

import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoDTO {
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @DecimalMax("1000000000")
    @Digits(integer = 10, fraction = 0)
    private BigDecimal quantidade;

    @NotNull(message = "Informe uma chave unica para a operacao")
    private UUID chaveOperacao;

    @NotBlank(message = "Informe o motivo da movimentacao")
    @Size(min = 3, max = 500)
    private String motivo;
}
