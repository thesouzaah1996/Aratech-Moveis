package com.aratech.moveis.security.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.*;

import java.security.*;
import java.time.*;

public class ExcessoTentativas extends AuthenticationException {
    public ExcessoTentativas() {
        super("Não foi possível entrar. Aguarde e tente novamente.");
    }
}
