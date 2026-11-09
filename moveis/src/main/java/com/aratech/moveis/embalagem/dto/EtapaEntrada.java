package com.aratech.moveis.embalagem.dto;

import jakarta.validation.constraints.*;
public record EtapaEntrada(@Positive long esteiraId,@NotBlank @Size(max=150) String nome,
        @Size(max=1000) String instrucoes) {}
