package com.aratech.moveis.portaria.controleacesso.model;

import com.aratech.moveis.portaria.controleacesso.enums.StatusAcesso;

import jakarta.persistence.*;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registro_chegada")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroChegada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 44)
    private String notaFiscal;

    @Column(nullable = false, length = 150)
    private String empresa;

    @Column(nullable = false, length = 150)
    private String nomeMotorista;

    @Column(nullable = false, length = 7)
    private String placa;

    @Column(nullable = false, length = 1000)
    private String descricaoCarga;

    @Column(nullable = false, length = 30)
    private String setorResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusAcesso status = StatusAcesso.AGUARDANDO;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID chaveOperacao;

    @Column(nullable = false, updatable = false)
    private Instant dataChegada;

    @Column(length = 15)
    private String tipoOperacao;

    private Instant dataAutorizacao;
    private Instant dataEntrada;
    private Instant dataFinalizacao;
}
