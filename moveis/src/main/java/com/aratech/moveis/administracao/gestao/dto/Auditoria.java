package com.aratech.moveis.administracao.gestao.dto;

import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record Auditoria(
        Long id,
        String modulo,
        String acao,
        String entidade,
        Long registroId,
        Long usuarioId,
        String usuario,
        String detalhe,
        Instant criadoEm) {}
