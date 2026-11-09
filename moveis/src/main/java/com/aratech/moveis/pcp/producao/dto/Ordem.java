package com.aratech.moveis.pcp.producao.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record Ordem(
        Long id,
        String produto,
        int quantidade,
        LocalDate inicio,
        LocalDate previsao,
        String responsavel,
        Long linhaId,
        String linha,
        BigDecimal horasPrevistas,
        Long pedidoId,
        String status,
        Instant concluidoEm,
        Instant expedidoEm,
        long versao) {}
