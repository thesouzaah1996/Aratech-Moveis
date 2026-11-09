package com.aratech.moveis.fabricacao.dto;

import java.time.LocalDate;
public record LoteResumo(long id, String codigoModelo, String nomeModelo, int quantidade,
        LocalDate previsao, String status, long versao) {}
