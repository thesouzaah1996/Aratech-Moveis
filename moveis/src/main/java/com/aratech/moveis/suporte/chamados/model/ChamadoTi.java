package com.aratech.moveis.suporte.chamados.model;

import com.aratech.moveis.suporte.chamados.enums.PrioridadeTi;
import com.aratech.moveis.suporte.chamados.enums.StatusChamadoTi;
import com.aratech.moveis.suporte.chamados.enums.TipoChamadoTi;

import jakarta.persistence.*;

import lombok.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "suporte_ti_chamado")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoTi {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suporteTiChamadoSeq")
    @SequenceGenerator(
            name = "suporteTiChamadoSeq",
            sequenceName = "suporte_ti_chamado_seq",
            allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 40)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoChamadoTi tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrioridadeTi prioridade;

    @Column(nullable = false, length = 100)
    private String moduloAfetado;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String solicitante;

    @Column(nullable = false, length = 100)
    private String setor;

    @Column(nullable = false, length = 11)
    private String contato;

    @Column(nullable = false, length = 3000)
    private String descricao;

    @Column(nullable = false, length = 2000)
    private String tentativasSolucao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusChamadoTi status = StatusChamadoTi.ABERTO;

    @Column(nullable = false, updatable = false)
    private Instant dataAbertura;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID chaveOperacao;

    @PrePersist
    public void definirNumero() {
        numero =
                "TI-"
                        + dataAbertura.atZone(ZoneId.of("America/Sao_Paulo")).getYear()
                        + "-"
                        + String.format(Locale.ROOT, "%04d", id);
    }
}
