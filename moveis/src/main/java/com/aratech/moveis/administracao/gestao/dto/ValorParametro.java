package com.aratech.moveis.administracao.gestao.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record ValorParametro(
        @NotNull @Min(0) @Max(90) Integer valor,
        @NotNull UUID chaveOperacao,
        @NotNull @Min(0) Long versao) {}
