package com.aratech.moveis.rh.afastamentos.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record Afastamento(
        Long id,
        Long funcionarioId,
        String funcionario,
        String tipo,
        LocalDate inicio,
        LocalDate fim,
        String motivo,
        String status,
        String decisao,
        long versao) {}
