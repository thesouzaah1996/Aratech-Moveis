package com.aratechmoveis.recursoshumanos.perfil.service;


import com.aratechmoveis.recursoshumanos.perfil.dto.PerfilDTO;
import com.aratechmoveis.recursoshumanos.response.Response;

public interface PerfilService {
    Response criarPerfil(PerfilDTO perfilDTO);
    Response getPerfis();
    Response updatePerfil(Long id, PerfilDTO perfilDTO);
    Response ativarPerfil(Long id);
    Response desativarPerfil(Long id);
}
