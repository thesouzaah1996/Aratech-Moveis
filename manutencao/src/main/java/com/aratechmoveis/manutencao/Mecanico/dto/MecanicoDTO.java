package com.aratechmoveis.manutencao.Mecanico.dto;

import com.aratechmoveis.manutencao.Mecanico.entity.Especialidade;
import com.aratechmoveis.manutencao.Mecanico.entity.Turno;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MecanicoDTO {

    @NotBlank(message = "O nome do mecânico não pode estar vazio.")
    @Size(min = 2, max = 150, message = "O nome precisa ter entre 2 e 150 caracteres.")
    private String nome;

    @NotEmpty(message = "Informe pelo menos uma especialidade do mecânico.")
    private List<Especialidade> especialidades;

    @NotNull(message = "O turno do mecânico é obrigatório.")
    private Turno turno;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean ativo;
}
