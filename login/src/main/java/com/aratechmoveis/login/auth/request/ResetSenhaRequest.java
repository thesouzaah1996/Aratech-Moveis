package com.aratechmoveis.login.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetSenhaRequest {

    @NotBlank(message = "Código é obrigatório")
    private String codigo;

    @NotBlank(message = "Nova senha é obrigatória")
    private String novaSenha;
}
