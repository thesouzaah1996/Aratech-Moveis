package com.aratechmoveis.manutencao.pecaestoque.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "pecas_estoque", indexes = {
        @Index(name = "idx_pecas_estoque_nome", columnList = "nome"),
        @Index(name = "idx_pecas_estoque_localizacao", columnList = "localizacao")
})
public class PecaEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome da peça é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O código é obrigatório")
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Min(value = 0, message = "A quantidade não pode ser negativa")
    @Column(nullable = false)
    private Integer quantidade;

    @NotBlank(message = "A unidade é obrigatória")
    @Column(nullable = false, length = 10)
    private String unidade;

    @NotBlank(message = "A localização é obrigatória")
    @Column(nullable = false, length = 100)
    private String localizacao;

    @Column(length = 500)
    private String descricao;
}
