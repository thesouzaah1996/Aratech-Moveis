package com.aratech.moveis.assistencia.estoque.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class PecaAssistenciaException extends RuntimeException {
    private final HttpStatus status;

    public PecaAssistenciaException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
