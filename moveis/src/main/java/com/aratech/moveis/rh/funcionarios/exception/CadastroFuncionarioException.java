package com.aratech.moveis.rh.funcionarios.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class CadastroFuncionarioException extends RuntimeException {

    private final HttpStatus status;

    public CadastroFuncionarioException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
