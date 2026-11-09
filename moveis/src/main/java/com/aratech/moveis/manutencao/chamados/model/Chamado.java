package com.aratech.moveis.manutencao.chamados.model;

import com.aratech.moveis.manutencao.chamados.enums.OrigemChamado;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.enums.TipoManutencao;
import com.aratech.moveis.manutencao.mecanicos.model.Mecanico;
import com.aratech.moveis.manutencao.pecas.enums.FinalidadePeca;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "manutencao_chamado")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chamado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String equipamento;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private TipoManutencao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private OrigemChamado origem = OrigemChamado.MANUTENCAO;

    @Column(length = 150)
    private String nomePeca;

    @Column(length = 100)
    private String codigoPeca;

    @Column(precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(length = 5)
    private String unidade;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private FinalidadePeca finalidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrioridadeChamado prioridade;

    @Column(nullable = false, length = 150)
    private String solicitante;

    @Column(nullable = false, length = 100)
    private String setor;

    @Column(length = 11)
    private String telefone;

    @Column(nullable = false, length = 3000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusChamado status = StatusChamado.ABERTA;

    @Column(nullable = false, updatable = false)
    private Instant dataAbertura;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID chaveOperacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mecanico_id")
    private Mecanico mecanico;

    private Instant dataInicio;
    private Instant dataConclusao;
}
