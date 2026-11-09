package com.aratech.moveis.rh.ponto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;

@Schema(description = "Escala semanal: informe sete dias, de segunda (1) a domingo (7). Folga usa seis strings vazias. Horas extras são autorizadas explicitamente, de 0 a 120 minutos por dia.")
public record EscalaPontoDTO(
        @Min(1) @Max(7) int diaSemana,
        @NotNull @Size(min = 6, max = 6) List<String> horarios,
        @Min(0) @Max(120) int extraMinutos) {}
