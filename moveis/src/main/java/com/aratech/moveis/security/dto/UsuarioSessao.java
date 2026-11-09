package com.aratech.moveis.security.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

public record UsuarioSessao(
        Long id,
        String nome,
        String email,
        boolean administrador,
        long versao,
        Instant autenticadoEm,
        Set<String> perfis,
        Set<String> permissoes,
        boolean trocarSenha)
        implements Serializable {
    public UsuarioSessao(
            Long id,
            String nome,
            String email,
            boolean administrador,
            long versao,
            Instant autenticadoEm,
            Set<String> perfis,
            Set<String> permissoes) {
        this(id, nome, email, administrador, versao, autenticadoEm, perfis, permissoes, false);
    }
}
