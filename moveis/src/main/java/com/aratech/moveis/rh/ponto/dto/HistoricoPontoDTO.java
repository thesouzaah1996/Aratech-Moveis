package com.aratech.moveis.rh.ponto.dto;

import java.time.*;
import java.util.*;

public record HistoricoPontoDTO(
        Long id,
        LocalDate data,
        String anteriores,
        String novos,
        String motivo,
        String autor,
        String alteradoEm) {}
