package com.aratech.moveis.suporte.chamados.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ChamadoTiException extends RuntimeException {
    private final HttpStatus status;

    public ChamadoTiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
