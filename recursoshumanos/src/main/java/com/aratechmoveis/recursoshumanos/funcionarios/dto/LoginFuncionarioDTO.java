package com.aratechmoveis.recursoshumanos.funcionarios.dto;

import com.aratechmoveis.recursoshumanos.perfil.entity.Perfil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginFuncionarioDTO {

    @NotNull(message = "O id não pode ser nullo. Para criar o login de usuário, precisamos do id.")
    private Long id;

    @NotBlank(message = "O e-mail corporativo é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    private String emailCorporativo;
}
