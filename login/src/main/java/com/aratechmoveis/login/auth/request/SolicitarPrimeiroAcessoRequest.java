package com.aratechmoveis.login.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SolicitarPrimeiroAcessoRequest {

    @NotBlank(message = "Email pessoal é obrigatório")
    private String emailPessoal;
}
