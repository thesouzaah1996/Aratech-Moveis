package com.aratech.moveis.comercial.pedidos.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record NovaVenda(
        @NotBlank @Size(max = 150) String cliente,
        @NotBlank @Size(max = 150) String produto,
        @NotNull @Min(1) @Max(1000000000) Integer quantidade,
        @NotNull @DecimalMin("0.01") @DecimalMax("999999.99") @Digits(integer = 6, fraction = 2)
                BigDecimal valorUnitario,
        @NotNull LocalDate entrega,
        @NotNull LocalDate vencimento,
        @NotNull UUID chaveOperacao,
        @Min(0) long versao) {}
