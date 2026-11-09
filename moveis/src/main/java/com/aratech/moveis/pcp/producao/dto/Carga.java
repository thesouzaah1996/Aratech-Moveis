package com.aratech.moveis.pcp.producao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Schema(
        description =
                "Capacidade por linha, em horas por dia; carga planejada distribuída igualmente"
                        + " pelos dias corridos do período da ordem, sem presumir calendário de"
                        + " turnos.")
public record Carga(
        Long id, String nome, BigDecimal horasDia, BigDecimal cargaHoje, BigDecimal saldoHoje) {}
