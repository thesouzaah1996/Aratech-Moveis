package com.aratechmoveis.portaria.exceptions;

public class RegistroNaoAutorizadoException extends RuntimeException {
    public RegistroNaoAutorizadoException(String message) {
        super(message);
    }
}
