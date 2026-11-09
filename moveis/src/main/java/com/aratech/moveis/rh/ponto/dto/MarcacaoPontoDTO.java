package com.aratech.moveis.rh.ponto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Schema(description = "Registro do próximo horário para o funcionário da sessão. Não recebe ID nem credenciais. A hora é sempre do servidor.")
public record MarcacaoPontoDTO(
        @NotNull @Schema(description = "Data exibida no relógio; deve ser hoje em America/Sao_Paulo", format = "date") LocalDate data,
        @PositiveOrZero @Schema(description = "Versão do dia consultado; 0 para dia sem registro") long versao) {}
