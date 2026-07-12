package com.aratechmoveis.portaria.controle_acesso.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Table(name = "registros_chegada")
public class RegistroChegada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SetorResponsavel setorResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusCaminhao status = StatusCaminhao.AGUARDANDO;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEntrada;
}
