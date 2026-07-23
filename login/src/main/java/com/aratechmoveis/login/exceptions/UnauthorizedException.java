package com.aratechmoveis.login.exceptions;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final boolean bloqueado;

    public UnauthorizedException(String message) {
        this(message, false);
    }

    public UnauthorizedException(String message, boolean bloqueado) {
        super(message);
        this.bloqueado = bloqueado;
    }
}
