package com.aratech.moveis.almoxarifado.categorias.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private int status;
    private String mensagem;
    private Long categoriaId;
    private String nome;
    private Boolean ativo;
    private CategoriaResumoDTO categoria;
    private List<CategoriaResumoDTO> categorias;
    private Integer pagina;
    private Integer tamanho;
    private Long total;
    private Map<String, String> erros;
}
