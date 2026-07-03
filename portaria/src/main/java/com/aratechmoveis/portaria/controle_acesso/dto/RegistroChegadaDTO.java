package com.aratechmoveis.portaria.controle_acesso.dto;

import com.aratechmoveis.portaria.controle_acesso.entity.SetorResponsavel;
import com.aratechmoveis.portaria.controle_acesso.entity.StatusCaminhao;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistroChegadaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "A nota fiscal é obrigatória.")
    @Size(min = 9, max = 50, message = "A nota fiscal deve ter entre 9 e 50 caracteres.")
    private String notaFiscal;

    @NotBlank(message = "O nome da empresa é obrigatório.")
    @Size(min = 2, max = 100, message = "O nome da empresa deve ter entre 2 e 100 caracteres.")
    private String empresa;

    @NotBlank(message = "O nome do motorista é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome do motorista deve ter entre 2 e 150 caracteres.")
    private String nomeMotorista;

    @NotBlank(message = "A placa do veículo é obrigatória.")
    @Size(min = 7, max = 10, message = "A placa deve ter entre 7 e 10 caracteres.")
    private String placa;

    @NotNull(message = "O setor responsável é obrigatório.")
    private SetorResponsavel setorResponsavel;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private StatusCaminhao status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataEntrada;
}
