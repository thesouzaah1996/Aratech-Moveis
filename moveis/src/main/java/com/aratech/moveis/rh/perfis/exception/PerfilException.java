package com.aratech.moveis.rh.perfis.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class PerfilException extends RuntimeException {
    private final HttpStatus status;

    public PerfilException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
