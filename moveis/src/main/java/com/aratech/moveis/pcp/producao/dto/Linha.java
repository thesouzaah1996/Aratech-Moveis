package com.aratech.moveis.pcp.producao.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record Linha(Long id, String nome, BigDecimal horasDia, boolean ativo, long versao) {}
