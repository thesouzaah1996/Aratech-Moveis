package com.aratech.moveis.manutencao.estoque.dto;

import jakarta.validation.constraints.*;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PecaEstoqueAtualizacaoDTO {
    @NotBlank(message = "Informe o nome da peca")
    @Size(min = 2, max = 150)
    private String nome;

    @NotBlank(message = "Informe o codigo")
    @Size(max = 100)
    private String codigo;

    @NotBlank
    @Pattern(regexp = "un|kg|L|m|cx|par", message = "Selecione uma unidade valida")
    private String unidade;

    @NotBlank(message = "Informe a localizacao")
    @Size(min = 2, max = 100)
    private String localizacao;

    @Size(max = 2000)
    private String descricao;

    @com.fasterxml.jackson.annotation.JsonAnySetter
    public void rejeitarCampoDesconhecido(String campo, Object valor) {
        throw new IllegalArgumentException(
                "Campo nao permitido. Quantidade deve usar entrada ou saida.");
    }
}
