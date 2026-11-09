package com.aratech.moveis.suporte.chamados.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int status;
    private String mensagem;
    private ChamadoTiResumoDTO chamado;
    private List<ChamadoTiResumoDTO> chamados;
    private Integer pagina;
    private Integer tamanho;
    private Long total;
}
