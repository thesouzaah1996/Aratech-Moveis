package com.aratech.moveis.portaria.controleacesso.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ControleAcessoException extends RuntimeException {
    private final HttpStatus status;

    public ControleAcessoException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
