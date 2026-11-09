package com.aratech.moveis.pcp.producao.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservaCapacidade(LocalDate inicio, LocalDate previsao, BigDecimal horasPrevistas) {}
