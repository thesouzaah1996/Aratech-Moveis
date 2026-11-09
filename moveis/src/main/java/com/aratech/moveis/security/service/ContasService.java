package com.aratech.moveis.security.service;

import com.aratech.moveis.security.dto.Credencial;
import com.aratech.moveis.security.dto.UsuarioSessao;

import java.time.Instant;
import java.util.*;

public interface ContasService {
    public static final long ADMIN_TECNICO_ID = 0L;

    Optional<Credencial> credencial(String email);

    UsuarioSessao carregar(Long id, Instant autenticadoEm);
}
