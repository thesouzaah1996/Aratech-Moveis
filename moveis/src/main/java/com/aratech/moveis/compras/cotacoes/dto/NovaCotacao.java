package com.aratech.moveis.compras.cotacoes.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record NovaCotacao(
        @NotNull @Positive Long pedidoId,
        @NotBlank @Size(max = 150) String fornecedor1,
        @NotNull
                @DecimalMin("0.01")
                @DecimalMax("9999999999999.99")
                @Digits(integer = 13, fraction = 2)
                BigDecimal valor1,
        @Size(max = 150) String fornecedor2,
        @DecimalMin("0.01") @DecimalMax("9999999999999.99") @Digits(integer = 13, fraction = 2)
                BigDecimal valor2,
        @Size(max = 150) String fornecedor3,
        @DecimalMin("0.01") @DecimalMax("9999999999999.99") @Digits(integer = 13, fraction = 2)
                BigDecimal valor3,
        @NotNull UUID chaveOperacao,
        @Min(0) long versao) {}
