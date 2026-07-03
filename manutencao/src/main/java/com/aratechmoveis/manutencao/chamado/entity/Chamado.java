package com.aratechmoveis.manutencao.chamado.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "chamados")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O equipamento é obrigatório")
    @Column(nullable = false, length = 150)
    private String equipamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoManutencao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridade prioridade;

    @NotBlank(message = "O solicitante é obrigatório")
    @Column(nullable = false, length = 150)
    private String solicitante;

    @NotBlank(message = "O setor é obrigatório")
    @Column(nullable = false, length = 100)
    private String setor;

    @Column(length = 16)
    private String telefone;

    @NotBlank(message = "A descrição é obrigatória")
    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(length = 150)
    private String mecanico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusChamado status = StatusChamado.ABERTA;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAbertura;
}
