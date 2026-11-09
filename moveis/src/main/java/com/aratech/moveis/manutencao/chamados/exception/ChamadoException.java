package com.aratech.moveis.manutencao.chamados.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ChamadoException extends RuntimeException {
    private final HttpStatus status;

    public ChamadoException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
