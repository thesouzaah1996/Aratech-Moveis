package com.aratechmoveis.manutencao.solicitacaopeca.dto;

import com.aratechmoveis.manutencao.chamado.entity.Prioridade;
import com.aratechmoveis.manutencao.solicitacaopeca.entity.FinalidadePeca;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolicitacaoPecaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome da peça é obrigatório.")
    @Size(max = 150, message = "O nome da peça deve ter no máximo 150 caracteres.")
    private String nomePeca;

    @Size(max = 50, message = "O código deve ter no máximo 50 caracteres.")
    private String codigo;

    @NotNull(message = "A quantidade é obrigatória.")
    @Min(value = 1, message = "A quantidade deve ser maior que zero.")
    @Max(value = 1_000_000, message = "A quantidade não pode ultrapassar 1.000.000.")
    private Integer quantidade;

    @NotBlank(message = "A unidade é obrigatória.")
    @Size(max = 10, message = "A unidade deve ter no máximo 10 caracteres.")
    private String unidade;

    @NotBlank(message = "O equipamento é obrigatório.")
    @Size(max = 150, message = "O equipamento deve ter no máximo 150 caracteres.")
    private String equipamento;

    @NotNull(message = "A finalidade é obrigatória.")
    private FinalidadePeca finalidade;

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

    @NotBlank(message = "As observações são obrigatórias.")
    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    private String observacoes;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime criadoEm;
}
