package com.aratechmoveis.login.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email
    private String emailCorporativo;

    @NotBlank(message = "Senha é obrigatório")
    private String senha;
}
