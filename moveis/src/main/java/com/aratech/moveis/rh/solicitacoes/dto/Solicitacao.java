package com.aratech.moveis.rh.solicitacoes.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record Solicitacao(
        Long id,
        Long funcionarioId,
        String funcionario,
        String tipo,
        String descricao,
        String status,
        String resposta,
        Instant criadoEm,
        long versao) {}
