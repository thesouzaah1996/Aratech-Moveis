package com.aratechmoveis.login.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrimeiroAcessoRequest {

    @NotNull(message = "id do funcionário é obrigatório")
    private Long idFuncionario;

    @NotBlank(message = "Email pessoal é obrigatório")
    private String emailCorporativo;

    @NotBlank(message = "Código é obrigatório")
    private String codigo;

    @NotBlank(message = "Nova senha é obrigatória")
    private String novaSenha;
}
