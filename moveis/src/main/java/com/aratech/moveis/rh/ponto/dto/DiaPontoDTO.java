package com.aratech.moveis.rh.ponto.dto;

import java.time.*;
import java.util.*;

public record DiaPontoDTO(LocalDate data, List<String> horarios, long versao, boolean ajustado,
        @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"SEM_MARCACAO", "SEM_ESCALA", "NO_HORARIO", "FORA_HORARIO"}, description = "Avaliação dos horários registrados em relação à escala atual, ao minuto") String situacao,
        @io.swagger.v3.oas.annotations.media.Schema(description = "Alerta de escala ausente ou limite de horas excedido. A marcação é preservada") String aviso,
        @io.swagger.v3.oas.annotations.media.Schema(description = "Soma dos pares completos de entrada e saída, em minutos") int totalMinutos) {
    public DiaPontoDTO(LocalDate data, List<String> horarios, long versao, boolean ajustado) {
        this(data, horarios, versao, ajustado, "SEM_ESCALA", "", total(horarios));
    }
    private static int total(List<String> horarios) {
        int total = 0;
        for (int i = 0; i < 6; i += 2)
            if (!horarios.get(i).isEmpty() && !horarios.get(i + 1).isEmpty())
                total += (int) Duration.between(LocalTime.parse(horarios.get(i)), LocalTime.parse(horarios.get(i + 1))).toMinutes();
        return total;
    }
}
