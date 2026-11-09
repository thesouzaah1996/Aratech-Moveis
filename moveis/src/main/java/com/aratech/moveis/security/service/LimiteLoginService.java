package com.aratech.moveis.security.service;

import org.springframework.transaction.annotation.*;

import java.security.*;
import java.time.*;

public interface LimiteLoginService {

    void consumir(String tipo, String valor, int limite);

    void limparConta(String email);
}
