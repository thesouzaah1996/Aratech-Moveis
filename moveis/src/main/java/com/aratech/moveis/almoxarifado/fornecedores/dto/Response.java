package com.aratech.moveis.almoxarifado.fornecedores.dto;

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
    private Long fornecedorId;
    private String nome;
    private Boolean ativo;
    private FornecedorResumoDTO fornecedor;
    private List<FornecedorResumoDTO> fornecedores;
    private Integer pagina;
    private Integer tamanho;
    private Long total;
    private Map<String, String> erros;
}
