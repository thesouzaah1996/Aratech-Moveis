package com.aratechmoveis.recursoshumanos.perfil.service;


import com.aratechmoveis.recursoshumanos.perfil.dto.PerfilDTO;
import com.aratechmoveis.recursoshumanos.response.Response;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface PerfilService {
    Response criarPerfil(PerfilDTO perfilDTO);
    Response listarPerfis();
    Response atualizarPerfil(Long id, PerfilDTO perfilDTO);
    Response ativarPerfil(Long id);
    Response desativarPerfil(Long id);
}
