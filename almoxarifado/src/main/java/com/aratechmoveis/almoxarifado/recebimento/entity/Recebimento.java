package com.aratechmoveis.almoxarifado.recebimento.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Table(name = "recebimentos")
public class Recebimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "A nota fiscal é obrigatória")
    @Column(nullable = false, unique = true, length = 50)
    private String notaFiscal;

    @NotBlank(message = "O nome da empresa é obrigatório")
    @Size(min = 2, max = 100, message = "O nome da empresa deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String empresa;

    @NotBlank(message = "O nome do motorista é obrigatório")
    @Size(min = 2, max = 150, message = "O nome do motorista deve ter entre 2 e 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nomeMotorista;

    @NotBlank(message = "A placa do veículo é obrigatória")
    @Column(nullable = false, length = 10)
    private String placa;

    @NotBlank(message = "A descrição da carga é obrigatória")
    @Column(nullable = false)
    private String descricaoCarga;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusRecebimento statusRecebimento = StatusRecebimento.AGUARDANDO;

    @NotBlank(message = "Setor responsável é obrigatório")
    @Column(nullable = false)
    private String setorResponsavel;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataRecebimento;
}
