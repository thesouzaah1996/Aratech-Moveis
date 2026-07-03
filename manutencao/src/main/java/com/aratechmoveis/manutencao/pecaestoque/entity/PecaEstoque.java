package com.aratechmoveis.manutencao.pecaestoque.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "pecas_estoque")
public class PecaEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da peça é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O código é obrigatório")
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

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
