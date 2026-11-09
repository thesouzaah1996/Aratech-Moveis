package com.aratech.moveis.almoxarifado.produtos.model;

import com.aratech.moveis.almoxarifado.produtos.enums.TipoMovimentacao;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "movimentacao_estoque",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_movimentacao_chave", columnNames = "chave_operacao"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false, updatable = false)
    private Integer quantidade;

    @Column(nullable = false, updatable = false)
    private Integer saldoAnterior;

    @Column(nullable = false, updatable = false)
    private Integer saldoPosterior;

    @Column(nullable = false, updatable = false)
    private UUID chaveOperacao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(updatable = false)
    private Long responsavelId;

    @Column(nullable = false, updatable = false, length = 150)
    private String responsavelNome;

    @Column(nullable = false, updatable = false, length = 500)
    private String motivo;
}
