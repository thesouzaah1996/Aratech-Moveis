package com.aratechmoveis.recursoshumanos.perfil.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerfilDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome do perfil não pode estar vazio.")
    @Size(min = 2, max = 100, message = "O nome precisa ter entre 2 e 100 caracteres.")
    private String nome;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean ativo;
}
