package com.aratechmoveis.almoxarifado.fornecedor.dto;

import com.aratechmoveis.almoxarifado.fornecedor.model.Representante;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FornecedorDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome do fornecedor não pode estar vazio.")
    @Size(min = 2, max = 100, message = "O nome precisa ter entre 2 e 100 caracteres.")
    private String nome;

    @NotBlank(message = "Informe um e-mail válido para o fornecedor.")
    @Email(message = "O e-mail informado não tem um formato válido.")
    @Size(max = 150, message = "O e-mail não pode ultrapassar 150 caracteres.")
    private String email;

    @NotBlank(message = "O telefone fixo do fornecedor é obrigatório.")
    @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?\\d{4}-?\\d{4}$", message = "Informe um telefone fixo válido, no formato (XX) XXXX-XXXX.")
    private String telefone;

    @Valid
    private Representante representante;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean ativo;
}
