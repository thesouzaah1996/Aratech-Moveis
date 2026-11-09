package com.aratech.moveis.assistencia.estoque.dto;

import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentoAssistenciaDTO {
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @DecimalMax("999999999999.999")
    @Digits(integer = 12, fraction = 3)
    private BigDecimal quantidade;

    @NotNull(message = "Informe uma chave unica para a operacao")
    private UUID chaveOperacao;

    @NotBlank(message = "Informe o motivo da movimentacao")
    @Size(min = 3, max = 500)
    private String motivo;
}
