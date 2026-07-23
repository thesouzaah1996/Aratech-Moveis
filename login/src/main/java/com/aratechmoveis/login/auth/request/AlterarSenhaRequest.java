package com.aratechmoveis.login.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlterarSenhaRequest {

    @NotBlank(message = "Código é obrigatório")
    private String codigo;

    @NotBlank(message = "Email é obrigatório")
    @Email
    private String emailCorporativo;

    @NotBlank(message = "Senha é obrigatório")
    private String senha;

    @NotBlank(message = "Confirmar a senha é obrigatório ")
    private String confirmarSenha;
}
