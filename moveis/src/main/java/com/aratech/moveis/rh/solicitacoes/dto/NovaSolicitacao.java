package com.aratech.moveis.rh.solicitacoes.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record NovaSolicitacao(
        @NotNull @Positive Long funcionarioId,
        @NotBlank @Size(max = 60) String tipo,
        @NotBlank @Size(min = 3, max = 1000) String descricao,
        @NotNull UUID chaveOperacao,
        @Min(0) long versao) {}
