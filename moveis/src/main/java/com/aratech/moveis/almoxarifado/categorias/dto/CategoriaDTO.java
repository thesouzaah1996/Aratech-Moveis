package com.aratech.moveis.almoxarifado.categorias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoriaDTO {

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(min = 2, max = 100)
    private String nome;
}
