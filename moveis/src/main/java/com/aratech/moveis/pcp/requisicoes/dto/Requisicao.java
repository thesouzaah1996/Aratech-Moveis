package com.aratech.moveis.pcp.requisicoes.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record Requisicao(
        Long id,
        Long ordemId,
        String produtoFinal,
        Long produtoId,
        String material,
        int quantidade,
        String motivo,
        String status,
        String resposta,
        long versao) {}
