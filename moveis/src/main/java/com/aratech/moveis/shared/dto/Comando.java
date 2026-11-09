package com.aratech.moveis.shared.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record Comando(
        @NotNull UUID chaveOperacao,
        @NotNull @Min(0) Long versao,
        @Size(max = 1000) String motivo,
        LocalDate data,
        Long referenciaId,
        Integer escolha) {}
