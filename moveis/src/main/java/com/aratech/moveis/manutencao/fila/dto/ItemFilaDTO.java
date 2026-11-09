package com.aratech.moveis.manutencao.fila.dto;

import com.aratech.moveis.manutencao.chamados.enums.OrigemChamado;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.enums.TipoManutencao;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.mecanicos.dto.MecanicoResumoDTO;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaResumoDTO;

import java.time.Instant;

public record ItemFilaDTO(
        Long id,
        OrigemChamado origem,
        String equipamento,
        TipoManutencao tipo,
        PrioridadeChamado prioridade,
        String solicitante,
        String setor,
        String telefone,
        String descricao,
        StatusChamado status,
        Instant dataAbertura,
        MecanicoResumoDTO mecanico,
        Instant dataInicio,
        Instant dataConclusao,
        SolicitacaoPecaResumoDTO peca) {
    public static ItemFilaDTO from(Chamado c) {
        return new ItemFilaDTO(
                c.getId(),
                c.getOrigem(),
                c.getEquipamento(),
                c.getTipo(),
                c.getPrioridade(),
                c.getSolicitante(),
                c.getSetor(),
                c.getTelefone(),
                c.getDescricao(),
                c.getStatus(),
                c.getDataAbertura(),
                c.getMecanico() == null ? null : MecanicoResumoDTO.from(c.getMecanico()),
                c.getDataInicio(),
                c.getDataConclusao(),
                c.getOrigem() == OrigemChamado.PECA ? SolicitacaoPecaResumoDTO.from(c) : null);
    }
}
