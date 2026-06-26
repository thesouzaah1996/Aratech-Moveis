package com.aratechmoveis.login.perfil.service;

import com.aratechmoveis.login.perfil.dto.PerfilDTO;
import com.aratechmoveis.login.perfil.model.Perfil;
import com.aratechmoveis.login.res.Response;

import java.util.List;

public interface PerfilService {

    Response<PerfilDTO> createPerfil(PerfilDTO perfil);

    Response<PerfilDTO> updatePerfil(Long id, PerfilDTO perfilRequest);

    Response<List<PerfilDTO>> getTodosPerfis();

    Response<?> enablePerfil(Long id);

    Response<?> disablePerfil(Long id);
}
