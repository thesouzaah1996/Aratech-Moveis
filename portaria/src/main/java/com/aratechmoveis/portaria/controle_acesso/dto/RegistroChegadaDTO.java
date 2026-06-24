package com.aratechmoveis.portaria.controle_acesso.dto;

import com.aratechmoveis.portaria.controle_acesso.model.SetorResponsavel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistroChegadaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "A nota fiscal é obrigatória.")
    @Size(min = 9,max = 50, message = "A nota fiscal deve ter entre 9 e 50 caracteres.")
    private String notaFiscal;

    @NotNull(message = "O nome da empresa é obrigatório.")
    private String empresa;

    @NotNull(message = "O nome do motorista é obrigatório.")
    private String nomeMotorista;

    @NotNull(message = "O númeor da placa é obrigatório.")
    private String placa;

    @NotNull(message = "O setor responsável é obrigatório.")
    private SetorResponsavel setorResponsavel;
}
