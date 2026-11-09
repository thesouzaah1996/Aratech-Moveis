package com.aratech.moveis.financeiro.titulos.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record Titulo(
        Long id,
        String tipo,
        String descricao,
        String pessoa,
        BigDecimal valor,
        LocalDate vencimento,
        String documento,
        LocalDate emissao,
        String referencia,
        String origem,
        Long origemId,
        String status,
        LocalDate baixa,
        long versao) {}
