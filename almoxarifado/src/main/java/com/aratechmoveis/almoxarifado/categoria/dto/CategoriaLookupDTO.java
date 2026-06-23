package com.aratechmoveis.almoxarifado.categoria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaLookupDTO {
    private Long id;
    private String nome;
}
