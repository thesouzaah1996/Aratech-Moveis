package com.aratech.moveis.assistencia.estoque.model;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "assistencia_peca_estoque")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PecaAssistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 100, unique = true)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String modelo;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidadeMinima;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(nullable = false, length = 5)
    private String unidade;

    @Column(nullable = false, length = 100)
    private String localizacao;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;
}
