package com.aratech.moveis.almoxarifado.categorias.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class CategoriaException extends RuntimeException {
    private final HttpStatus status;

    public CategoriaException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
