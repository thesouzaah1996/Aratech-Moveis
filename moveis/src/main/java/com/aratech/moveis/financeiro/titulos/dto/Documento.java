package com.aratech.moveis.financeiro.titulos.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record Documento(
        @NotBlank @Size(max = 100) String documento,
        @NotNull LocalDate emissao,
        @NotNull UUID chaveOperacao,
        @NotNull @Min(0) Long versao) {}
