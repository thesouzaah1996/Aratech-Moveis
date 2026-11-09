package com.aratech.moveis.embalagem.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description="Posto de embalagem. Capacidade é o número de operações simultâneas, incluindo pausadas; não é velocidade nem produção por hora. Atividade descreve o trabalho realizado nessa esteira.")
public record EsteiraEntrada(@NotBlank @Size(max=40) String codigo,
        @NotBlank @Size(max=120) String nome,@NotBlank @Size(max=150) String atividade,
        @Size(max=150) String responsavel,@Min(1) @Max(20) int capacidade,boolean ativo,
        @Size(max=1000) String observacoes,@PositiveOrZero long versao) {}
