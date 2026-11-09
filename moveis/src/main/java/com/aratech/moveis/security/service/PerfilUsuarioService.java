package com.aratech.moveis.security.service;

import com.aratech.moveis.security.dto.UsuarioSessao;

import org.springframework.web.bind.annotation.*;

import java.util.*;

public interface PerfilUsuarioService {

    Map<String, Object> meuPerfil(UsuarioSessao usuario);
}
