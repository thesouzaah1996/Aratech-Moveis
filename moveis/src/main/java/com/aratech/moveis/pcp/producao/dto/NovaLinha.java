package com.aratech.moveis.pcp.producao.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record NovaLinha(
        @NotBlank @Size(max = 100) String nome,
        @NotNull @DecimalMin("0.01") @DecimalMax("24") @Digits(integer = 2, fraction = 2)
                BigDecimal horasDia,
        @NotNull UUID chaveOperacao,
        @Min(0) long versao) {}
