package com.aratech.moveis.manutencao.estoque.dto;

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
    private PecaEstoqueResumoDTO peca;
    private List<PecaEstoqueResumoDTO> pecas;
    private List<MovimentacaoPecaResumoDTO> movimentacoes;
    private Integer pagina;
    private Integer tamanho;
    private Long total;
    private Map<String, String> erros;
}
