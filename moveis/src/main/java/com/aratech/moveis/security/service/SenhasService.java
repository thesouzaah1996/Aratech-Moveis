package com.aratech.moveis.security.service;

import com.aratech.moveis.security.dto.UsuarioSessao;

import java.util.*;

public interface SenhasService {

    String criarAcessoCorporativo(Long id, String email);

    String criarAcesso(Long id);

    void alterar(UsuarioSessao usuario, String atual, String nova);
}
