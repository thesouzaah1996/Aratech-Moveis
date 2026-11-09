package com.aratech.moveis.fabricacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Lote planejado com cópia imutável da ficha e das referências de fotos. Alterações posteriores no catálogo não mudam as peças deste lote. UUID permite repetir criação sem duplicar; conteúdo divergente retorna 409.")
public record NovoLoteCorte(@Positive long modeloId, @Min(1) @Max(10000) int quantidade,
        @NotNull LocalDate previsao, @Size(max=1000) String observacoes,
        @NotNull UUID chaveOperacao) {}
