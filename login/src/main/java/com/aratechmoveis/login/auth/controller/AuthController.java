package com.aratechmoveis.login.auth.controller;

import com.aratechmoveis.login.auth.request.PrimeiroAcessoRequest;
import com.aratechmoveis.login.auth.service.AuthService;
import com.aratechmoveis.login.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("aratech/login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("primeiro-acesso")
    public ResponseEntity<Response> primeiroAcesso(@RequestBody PrimeiroAcessoRequest primeiroAcessoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.definirSenhaPrimeiroAcesso(primeiroAcessoRequest));
    }
}
