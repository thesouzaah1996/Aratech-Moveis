package com.aratech.moveis.assistencia.estoque.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int status;
    private String mensagem;
    private PecaAssistenciaResumoDTO peca;
    private List<PecaAssistenciaResumoDTO> pecas;
    private List<MovimentoAssistenciaResumoDTO> movimentacoes;
    private Integer pagina;
    private Integer tamanho;
    private Long total;
    private Long totalAbaixoMinimo;
    private Map<String, String> erros;
}
