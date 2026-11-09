package com.aratech.moveis.security.model;

public record DadosConta(
        Long id,
        String nome,
        String email,
        boolean administrador,
        long versao,
        boolean trocarSenha) {}
