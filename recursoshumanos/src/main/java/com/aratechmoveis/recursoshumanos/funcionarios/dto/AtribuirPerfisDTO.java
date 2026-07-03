package com.aratechmoveis.recursoshumanos.funcionarios.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtribuirPerfisDTO {

    @NotEmpty(message = "Selecione ao menos um perfil.")
    private List<Long> perfisIds;
}
