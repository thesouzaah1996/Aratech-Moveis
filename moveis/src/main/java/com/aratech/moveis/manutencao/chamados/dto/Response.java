package com.aratech.moveis.manutencao.chamados.dto;

import com.aratech.moveis.manutencao.fila.dto.ItemFilaDTO;
import com.aratech.moveis.manutencao.mecanicos.dto.MecanicoResumoDTO;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaResumoDTO;
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
    private ChamadoResumoDTO chamado;
    private SolicitacaoPecaResumoDTO solicitacao;
    private List<ChamadoResumoDTO> chamados;
    private MecanicoResumoDTO mecanico;
    private List<MecanicoResumoDTO> mecanicos;
    private ItemFilaDTO item;
    private List<ItemFilaDTO> itens;
    private Integer pagina;
    private Integer tamanho;
    private Long total;
}
