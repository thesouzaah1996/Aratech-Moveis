package com.aratechmoveis.login.perfil.service;

import com.aratechmoveis.login.response.Response;

import java.util.List;

public interface Perfil {

    Response<Perfil> addPerfil(Perfil perfil);

    Response<Perfil> updatePerfil(Long id, Perfil perfil);

    Response<List<Perfil>> getPerfis();

    Response<?> desativarPerfil(Long id);

    Response<?> ativarPerfil(Long id);
}
