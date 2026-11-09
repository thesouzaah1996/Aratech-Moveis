package com.aratech.moveis.assistencia.lotes.model;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.*;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "assistencia_lote")
@Getter
@Setter
@NoArgsConstructor
public class LoteAssistencia {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description =
                    "Referência numérica única da solicitação administrativa. Repetição exige os"
                            + " mesmos dados.")
    @Column(unique = true, nullable = false)
    private Long origemId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    private String codigo;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    private String produto;

    @Schema(maxLength = 255)
    private String empresa;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
    private int quantidade;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate dataEntrada;

    @Schema(maxLength = 255)
    private String responsavel;

    @Schema(maxLength = 255)
    private String defeito;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String status;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant iniciadoEm;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant finalizadoEm;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant carregadoEm;

    @Version private Long versao;
}
