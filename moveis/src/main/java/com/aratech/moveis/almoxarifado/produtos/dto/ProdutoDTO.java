package com.aratech.moveis.almoxarifado.produtos.dto;

import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDTO {
    @NotBlank(message = "Informe o nome do produto")
    @Size(min = 2, max = 150)
    private String nome;

    @NotBlank(message = "Informe o SKU")
    @Pattern(
            regexp = "[A-Z0-9_-]{1,64}",
            message = "SKU deve ter de 1 a 64 letras, numeros, hifen ou sublinhado")
    private String sku;

    @NotNull(message = "Informe a categoria")
    @Positive
    private Long categoriaID;

    @NotNull(message = "Informe o fornecedor")
    @Positive
    private Long fornecedorID;

    @NotBlank(message = "Informe o local de armazenamento")
    @Size(min = 2, max = 100)
    private String localArmazenamento;

    @Size(max = 2000)
    private String descricao;

    private LocalDateTime vencimentoProduto;

    @NotNull(message = "Informe a quantidade inicial")
    @DecimalMin("0")
    @DecimalMax("1000000000")
    @Digits(integer = 10, fraction = 0)
    private BigDecimal quantidade;
}
