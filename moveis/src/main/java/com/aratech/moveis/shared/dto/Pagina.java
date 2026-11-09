package com.aratech.moveis.shared.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record Pagina<T>(List<T> itens, long total, int pagina, int tamanho) {}
