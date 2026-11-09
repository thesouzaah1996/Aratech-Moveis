package com.aratech.moveis.almoxarifado.fornecedores.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class FornecedorException extends RuntimeException {
    private final HttpStatus status;

    public FornecedorException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
