package com.aratech.moveis.fabricacao.dto;

import java.time.*;
public record LoteCorte(long id, long modeloId, long modeloVersao, int quantidade,
        LocalDate previsao, String observacoes, String status, long versao,
        ModeloGuardaRoupa modelo, String autor, Instant criadoEm, Instant enviadoEm,
        Instant iniciadoEm, Instant concluidoEm) {}
