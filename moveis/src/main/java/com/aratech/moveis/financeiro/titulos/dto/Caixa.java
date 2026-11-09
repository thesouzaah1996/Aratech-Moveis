package com.aratech.moveis.financeiro.titulos.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record Caixa(
        Long id,
        LocalDate data,
        String descricao,
        String tipo,
        BigDecimal valor,
        BigDecimal saldoAcumulado) {}
