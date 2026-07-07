package com.aratechmoveis.login.auth.entity;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigo_reset_senha")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodigoResetSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private boolean usado;

    private LocalDateTime dataExpiracao;

    @OneToOne(targetEntity = Funcionario.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Funcionario funcionario;
}
