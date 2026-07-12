package com.aratechmoveis.login.auth.controller;

import com.aratechmoveis.login.auth.request.LoginRequest;
import com.aratechmoveis.login.auth.request.PrimeiroAcessoRequest;
import com.aratechmoveis.login.auth.service.AuthService;
import com.aratechmoveis.login.response.LoginResponse;
import com.aratechmoveis.login.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("login/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/entrar")
    public ResponseEntity<Response<LoginResponse>> entrar(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginRequest));
    }

    @PostMapping("primeiro-acesso/solicitar")
    public ResponseEntity<Response> solicitarPrimeiroAcesso(@RequestParam String email) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.solicitarPrimeiroAcesso(email));
    }

    @PostMapping("primeiro-acesso/confirmar")
    public ResponseEntity<Response> confirmarPrimeiroAcesso(@RequestBody PrimeiroAcessoRequest primeiroAcessoRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.confirmarPrimeiroAcesso(primeiroAcessoRequest));
    }
}
