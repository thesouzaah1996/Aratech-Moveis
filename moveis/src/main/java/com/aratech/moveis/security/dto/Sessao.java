package com.aratech.moveis.security.dto;

import org.springframework.web.bind.annotation.*;

import java.util.*;

public record Sessao(
        Long id,
        String nome,
        String email,
        boolean administrador,
        Set<String> perfis,
        Set<String> permissoes,
        boolean trocarSenha) {}
