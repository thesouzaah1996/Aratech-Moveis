package com.aratech.moveis.rh.perfis.dto;

import org.springframework.data.domain.*;

import java.util.List;

public record UsuarioPerfilDTO(
        Long id, String nome, String email, String setor, List<PerfilResumoDTO> perfis) {}
