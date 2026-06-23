package com.aratechmoveis.almoxarifado.fornecedor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FornecedorLookupDTO {
    private Long id;
    private String nome;
}
