package com.aratech.moveis.administracao.gestao.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record Parametro(String chave, String descricao, int valor, long versao) {}
