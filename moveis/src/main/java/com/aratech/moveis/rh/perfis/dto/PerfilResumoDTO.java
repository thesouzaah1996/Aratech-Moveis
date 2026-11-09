package com.aratech.moveis.rh.perfis.dto;

import com.aratech.moveis.rh.perfis.model.Perfil;

public record PerfilResumoDTO(Long id, String nome) {
    public static PerfilResumoDTO from(Perfil perfil) {
        return new PerfilResumoDTO(perfil.getId(), perfil.getNome());
    }
}
