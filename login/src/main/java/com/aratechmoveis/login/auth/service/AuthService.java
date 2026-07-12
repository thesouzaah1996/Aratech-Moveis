package com.aratechmoveis.login.auth.service;

import com.aratechmoveis.login.auth.request.LoginRequest;
import com.aratechmoveis.login.auth.request.PrimeiroAcessoRequest;
import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.response.LoginResponse;
import com.aratechmoveis.login.response.Response;

public interface AuthService {

    Response<LoginResponse> login(LoginRequest loginRequest);

    Response<?> solicitarRedefinicaoSenha(String email);

    Response<?> solicitarPrimeiroAcesso(String emailPessoal);

    Response<?> confirmarPrimeiroAcesso(PrimeiroAcessoRequest primeiroAcessoRequest);
}
