package com.aratech.moveis.almoxarifado.produtos.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ProdutoException extends RuntimeException {
    private final HttpStatus status;

    public ProdutoException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
