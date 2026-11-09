package com.aratech.moveis.security.exception;

public class AuthException extends RuntimeException {
    private final int status;

    public AuthException(int status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
