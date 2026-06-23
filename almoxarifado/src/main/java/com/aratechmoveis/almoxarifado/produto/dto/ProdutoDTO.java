package com.aratechmoveis.almoxarifado.produto.dto;

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
public class ProdutoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "A categoria é obrigatória.")
    @Positive(message = "O ID da categoria deve ser um valor positivo.")
    private Long categoriaID;

    @NotBlank(message = "O nome do produto é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
    private String nome;

    @NotBlank(message = "O SKU é obrigatório.")
    @Size(max = 50, message = "O SKU deve ter no máximo 50 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "O SKU deve conter apenas letras, números, hífens e underscores.")
    private String sku;

    @NotNull(message = "A quantidade em estoque é obrigatória.")
    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa.")
    @Max(value = 1_000_000, message = "A quantidade em estoque não pode ultrapassar 1.000.000.")
    private Integer quantidade;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
    private String descricao;

    @NotNull(message = "Local de Armazenamento é obrigatório")
    private String localArmazenamento;

    @FutureOrPresent(message = "A data de vencimento deve ser uma data presente ou futura.")
    private LocalDateTime vencimentoProduto;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime criadoEm;

}
