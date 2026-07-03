package com.aratechmoveis.manutencao;

import com.aratechmoveis.manutencao.chamado.dto.ChamadoDTO;
import com.aratechmoveis.manutencao.pecaestoque.dto.PecaEstoqueDTO;
import com.aratechmoveis.manutencao.solicitacaopeca.dto.SolicitacaoPecaDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private int status;
    private String message;

    private ChamadoDTO chamado;
    private List<ChamadoDTO> chamados;

    private PecaEstoqueDTO pecaEstoque;
    private List<PecaEstoqueDTO> pecasEstoque;

    private SolicitacaoPecaDTO solicitacaoPeca;
    private List<SolicitacaoPecaDTO> solicitacoesPeca;

    private final LocalDateTime timestamp = LocalDateTime.now();
}
