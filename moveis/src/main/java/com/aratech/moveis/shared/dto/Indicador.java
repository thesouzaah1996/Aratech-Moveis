package com.aratech.moveis.shared.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record Indicador(String titulo, BigDecimal valor, String unidade, String descricao) {}
