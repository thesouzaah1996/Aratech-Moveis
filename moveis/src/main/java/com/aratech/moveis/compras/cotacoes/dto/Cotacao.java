package com.aratech.moveis.compras.cotacoes.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record Cotacao(
        Long id,
        Long pedidoId,
        String item,
        BigDecimal quantidade,
        String fornecedor1,
        BigDecimal valor1,
        String fornecedor2,
        BigDecimal valor2,
        String fornecedor3,
        BigDecimal valor3,
        Integer escolhido,
        String status,
        long versao) {}
