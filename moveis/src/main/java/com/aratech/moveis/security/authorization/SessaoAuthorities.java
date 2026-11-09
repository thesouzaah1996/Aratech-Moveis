package com.aratech.moveis.security.authorization;

import com.aratech.moveis.security.dto.UsuarioSessao;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

public final class SessaoAuthorities {
    private SessaoAuthorities() {}

    public static List<SimpleGrantedAuthority> authorities(UsuarioSessao usuario) {
        var authorities = new ArrayList<SimpleGrantedAuthority>();
        usuario.permissoes().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        // As roles refletem os perfis cadastrados; os endpoints exigem permissões explícitas.
        usuario.perfis().forEach(p -> authorities.add(new SimpleGrantedAuthority("ROLE_" + p)));
        return authorities;
    }
}
