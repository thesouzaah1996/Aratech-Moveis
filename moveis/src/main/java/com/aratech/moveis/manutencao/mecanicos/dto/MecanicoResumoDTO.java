package com.aratech.moveis.manutencao.mecanicos.dto;

import com.aratech.moveis.manutencao.mecanicos.model.Mecanico;

import java.util.List;

public record MecanicoResumoDTO(
        Long id, String nome, List<String> especialidades, String turno, boolean ativo) {
    public static MecanicoResumoDTO from(Mecanico m) {
        return new MecanicoResumoDTO(
                m.getId(),
                m.getNome(),
                m.getEspecialidades().stream().sorted().map(Enum::name).toList(),
                m.getTurno().name(),
                m.isAtivo());
    }
}
