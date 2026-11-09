package com.aratech.moveis.pcp.producao.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record EstadoOrdem(Long id, long versao, String status, Long pedidoId) {}
