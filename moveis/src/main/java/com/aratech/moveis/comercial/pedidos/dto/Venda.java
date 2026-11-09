package com.aratech.moveis.comercial.pedidos.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record Venda(
        Long id,
        String cliente,
        String produto,
        int quantidade,
        BigDecimal valorUnitario,
        LocalDate entrega,
        LocalDate vencimento,
        String status,
        long versao) {}
