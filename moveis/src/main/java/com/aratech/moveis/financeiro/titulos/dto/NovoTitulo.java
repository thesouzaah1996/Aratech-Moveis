package com.aratech.moveis.financeiro.titulos.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record NovoTitulo(
        @NotBlank @Size(max = 200) String descricao,
        @NotBlank @Size(max = 150) String pessoa,
        @NotNull
                @DecimalMin("0.01")
                @DecimalMax("9999999999999.99")
                @Digits(integer = 13, fraction = 2)
                BigDecimal valor,
        @NotNull LocalDate vencimento,
        @Size(max = 100) String documento,
        LocalDate emissao,
        @Size(max = 150) String referencia,
        @NotNull UUID chaveOperacao,
        @Min(0) long versao) {}
