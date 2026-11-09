package com.aratech.moveis.assistencia.estoque.model;

import com.aratech.moveis.assistencia.estoque.enums.TipoMovimentoAssistencia;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assistencia_movimentacao_estoque")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentoAssistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "peca_id", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PecaAssistencia peca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private TipoMovimentoAssistencia tipo;

    @Column(nullable = false, updatable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(nullable = false, updatable = false, precision = 15, scale = 3)
    private BigDecimal saldoAnterior;

    @Column(nullable = false, updatable = false, precision = 15, scale = 3)
    private BigDecimal saldoPosterior;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID chaveOperacao;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(nullable = false, updatable = false, length = 150)
    private String responsavelNome;

    @Column(nullable = false, updatable = false, length = 500)
    private String motivo;
}
