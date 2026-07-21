package com.aratechmoveis.manutencao.chamado.dto;

import com.aratechmoveis.manutencao.chamado.entity.Prioridade;
import com.aratechmoveis.manutencao.chamado.entity.StatusChamado;
import com.aratechmoveis.manutencao.chamado.entity.TipoManutencao;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChamadoDTO {

    @NotBlank(message = "O equipamento é obrigatório.")
    @Size(max = 150, message = "O equipamento deve ter no máximo 150 caracteres.")
    private String equipamento;

    @NotNull(message = "O tipo de manutenção é obrigatório.")
    private TipoManutencao tipo;

    @NotNull(message = "A prioridade é obrigatória.")
    private Prioridade prioridade;

    @NotBlank(message = "O solicitante é obrigatório.")
    @Size(max = 150, message = "O solicitante deve ter no máximo 150 caracteres.")
    private String solicitante;

    @NotBlank(message = "O setor é obrigatório.")
    @Size(max = 100, message = "O setor deve ter no máximo 100 caracteres.")
    private String setor;

    @Pattern(regexp = "^\\(\\d{2}\\) \\d{4,5}-\\d{4}$", message = "Telefone inválido.")
    private String telefone;

    @NotBlank(message = "A descrição é obrigatória.")
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
    private String descricao;

    @Size(max = 150, message = "O mecânico deve ter no máximo 150 caracteres.")
    private String mecanico;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private StatusChamado status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataAbertura;
}
