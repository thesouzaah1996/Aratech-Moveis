package com.aratechmoveis.login.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrimeiroAcessoRequest {

    @NotBlank(message = "Email pessoal é obrigatório")
    private String emailPessoal;

    @NotBlank(message = "Código é obrigatório")
    private String codigo;

    @NotBlank(message = "Nova senha é obrigatória")
    private String novaSenha;
}
