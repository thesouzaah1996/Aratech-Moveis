package com.aratech.moveis.rh.funcionarios.dto;

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

    private FuncionarioResumoDTO funcionario;

    private List<FuncionarioResumoDTO> funcionarios;

    private Integer pagina;

    private Integer tamanho;

    private Long total;

    private int status;

    private String mensagem;

    private Long funcionarioId;

    private String nomeCompleto;

    private Map<String, String> erros;

    private String token;

    //    private Permissao permissao;

    private Boolean ativo;

    private String TempoExpiracao;
}
