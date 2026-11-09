package com.aratech.moveis.manutencao.chamados.dto;

import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.enums.TipoManutencao;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.mecanicos.dto.MecanicoResumoDTO;

import java.time.Instant;

public record ChamadoResumoDTO(
        Long id,
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
        Instant dataConclusao) {
    public ChamadoResumoDTO(
            Long id,
            String equipamento,
            TipoManutencao tipo,
            PrioridadeChamado prioridade,
            String solicitante,
            String setor,
            String telefone,
            String descricao,
            StatusChamado status,
            Instant dataAbertura) {
        this(
                id,
                equipamento,
                tipo,
                prioridade,
                solicitante,
                setor,
                telefone,
                descricao,
                status,
                dataAbertura,
                null,
                null,
                null);
    }

    public static ChamadoResumoDTO from(Chamado c) {
        return new ChamadoResumoDTO(
                c.getId(),
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
                c.getDataConclusao());
    }
}
