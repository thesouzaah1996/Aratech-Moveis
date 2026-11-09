package com.aratech.moveis.fabricacao.dto;

import jakarta.validation.constraints.*;
public record AcaoLoteCorte(@Min(1) long versao) {}
