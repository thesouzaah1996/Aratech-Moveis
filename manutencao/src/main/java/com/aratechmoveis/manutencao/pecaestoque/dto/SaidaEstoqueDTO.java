package com.aratechmoveis.manutencao.pecaestoque.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaidaEstoqueDTO(
        @NotNull @Min(1) Integer quantidade
) {
}
