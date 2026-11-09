package com.aratech.moveis.pcp.producao.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record NovaOrdem(
        @NotBlank @Size(max = 150) String produto,
        @NotNull @Min(1) @Max(1000000000) Integer quantidade,
        @NotNull LocalDate inicio,
        @NotNull LocalDate previsao,
        @NotBlank @Size(max = 150) String responsavel,
        Long linhaId,
        @NotNull @DecimalMin("0.01") @DecimalMax("1000000") @Digits(integer = 7, fraction = 2)
                BigDecimal horasPrevistas,
        @NotNull UUID chaveOperacao,
        @Min(0) long versao) {}
