package com.aratechmoveis.manutencao.chamado.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtribuirMecanicoDTO {

    @NotBlank(message = "O mecânico é obrigatório.")
    @Size(max = 150, message = "O mecânico deve ter no máximo 150 caracteres.")
    private String mecanico;
}
