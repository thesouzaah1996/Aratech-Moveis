package com.aratechmoveis.portaria.controle_acesso.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "registro_chegada")
public class RegistroChegada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String notaFiscal;

    @NotBlank(message = "O nome da empresa é obrigatório")
    @Size(min = 2, max = 100, message = "O nome da empresa deve ter entre 2 e 100 caracteres ")
    private String empresa;

    @NotBlank(message = "O nome do motorista é obrigatório")
    @Size(min = 2, max = 150, message = "O nome do motorista deve ter entre 2 e 150 caracteres ")
    private String nomeMotorista;

    @NotBlank(message = "A placa do veiculo é obrigatória")
    @Size(min = 2, max = 150, message = "A placa do veículo deve ter entre 2 e 150 caracteres ")
    private String placa;

    @Enumerated(EnumType.STRING)
    private SetorResponsavel setorResponsavel;
}
