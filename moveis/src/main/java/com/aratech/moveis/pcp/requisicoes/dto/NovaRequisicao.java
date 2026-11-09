package com.aratech.moveis.pcp.requisicoes.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record NovaRequisicao(
        @NotNull @Positive Long ordemId,
        @NotNull @Positive Long produtoId,
        @NotNull @Min(1) @Max(1000000000) Integer quantidade,
        @NotBlank @Size(min = 3, max = 500) String motivo,
        @NotNull UUID chaveOperacao,
        @Min(0) long versao) {}
