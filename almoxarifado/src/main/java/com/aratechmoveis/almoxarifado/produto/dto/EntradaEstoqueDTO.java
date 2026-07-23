package com.aratechmoveis.almoxarifado.produto.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EntradaEstoqueDTO(
        @NotNull @Min(1) Integer quantidade
) {
}
