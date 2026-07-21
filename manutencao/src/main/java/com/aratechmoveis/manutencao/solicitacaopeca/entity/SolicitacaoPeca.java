package com.aratechmoveis.manutencao.solicitacaopeca.entity;

import com.aratechmoveis.manutencao.chamado.entity.Prioridade;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "solicitacoes_peca", indexes = {
        @Index(name = "idx_solicitacoes_peca_finalidade", columnList = "finalidade"),
        @Index(name = "idx_solicitacoes_peca_prioridade", columnList = "prioridade"),
        @Index(name = "idx_solicitacoes_peca_criado_em", columnList = "criadoEm")
})
public class SolicitacaoPeca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome da peça é obrigatório")
    @Column(nullable = false, length = 150)
    private String nomePeca;

    @Column(length = 50)
    private String codigo;

    @Min(value = 1, message = "A quantidade deve ser maior que zero")
    @Column(nullable = false)
    private Integer quantidade;

    @NotBlank(message = "A unidade é obrigatória")
    @Column(nullable = false, length = 10)
    private String unidade;

    @NotBlank(message = "O equipamento é obrigatório")
    @Column(nullable = false, length = 150)
    private String equipamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinalidadePeca finalidade;

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

    @NotBlank(message = "As observações são obrigatórias")
    @Column(nullable = false, length = 1000)
    private String observacoes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
