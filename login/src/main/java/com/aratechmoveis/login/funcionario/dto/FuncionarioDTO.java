package com.aratechmoveis.login.funcionario.dto;

import com.aratechmoveis.login.funcionario.entity.Perfil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FuncionarioDTO {

    @EqualsAndHashCode.Include
    @NotNull(message = "O ID do funcionário é obrigatório.")
    @Positive(message = "O ID do funcionário deve ser um valor positivo.")
    private Long idFuncionario;

    @NotBlank(message = "O nome do funcionário é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
    private String nomeFuncionario;

    @NotBlank(message = "O e-mail corporativo é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    private String emailCorporativo;

    private List<Perfil> perfis;

    private boolean ativo;
}
