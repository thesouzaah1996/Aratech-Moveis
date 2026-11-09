package com.aratech.moveis.rh.ponto.dto;

import java.time.*;
import java.util.*;

public record EdicaoPontoDTO(List<String> horarios, String motivo, long versao) {}
