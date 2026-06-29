package com.aratechmoveis.login.perfil.service;

import com.aratechmoveis.login.Response;
import com.aratechmoveis.login.perfil.dto.PerfilDTO;

public interface PerfilService {
    Response criarPerfil(PerfilDTO perfilDTO);
    Response getPerfis();
    Response updatePerfil(Long id, PerfilDTO perfilDTO);
    Response ativarPerfil(Long id);
    Response desativarPerfil(Long id);
}
