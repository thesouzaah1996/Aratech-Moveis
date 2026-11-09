package com.aratech.moveis.manutencao.estoque.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class PecaEstoqueException extends RuntimeException {
    private final HttpStatus status;

    public PecaEstoqueException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
