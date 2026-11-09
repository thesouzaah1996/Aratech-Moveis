package com.aratech.moveis.rh.afastamentos.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record NovoAfastamento(
        @NotNull @Positive Long funcionarioId,
        @NotBlank @Pattern(regexp = "FERIAS|LICENCA|AFASTAMENTO") String tipo,
        @NotNull LocalDate inicio,
        @NotNull LocalDate fim,
        @NotBlank @Size(min = 3, max = 500) String motivo,
        @NotNull UUID chaveOperacao,
        @Min(0) long versao) {}
