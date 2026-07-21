package com.aratechmoveis.manutencao.pecaestoque.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
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
public class PecaEstoqueDTO {

    @NotBlank(message = "O nome da peça é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
    private String nome;

    @NotBlank(message = "O código é obrigatório.")
    @Size(max = 50, message = "O código deve ter no máximo 50 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "O código deve conter apenas letras, números, hífens e underscores.")
    private String codigo;

    @NotNull(message = "A quantidade é obrigatória.")
    @Min(value = 0, message = "A quantidade não pode ser negativa.")
    @Max(value = 1_000_000, message = "A quantidade não pode ultrapassar 1.000.000.")
    private Integer quantidade;

    @NotBlank(message = "A unidade é obrigatória.")
    @Size(max = 10, message = "A unidade deve ter no máximo 10 caracteres.")
    private String unidade;

    @NotBlank(message = "A localização é obrigatória.")
    @Size(max = 100, message = "A localização deve ter no máximo 100 caracteres.")
    private String localizacao;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
    private String descricao;
}
