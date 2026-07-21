package com.aratechmoveis.login.auth.entity;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigos_reset_senha", indexes = {
        @Index(name = "idx_codigo_reset_senha_funcionario", columnList = "id_funcionario")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CodigoResetSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Exclude
    @Column(nullable = false, unique = true, length = 64)
    private String codigo;

    @Column(nullable = false)
    private boolean usado;

    @Column(nullable = false)
    private LocalDateTime dataExpiracao;

    @ToString.Exclude
    @OneToOne(targetEntity = Funcionario.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_funcionario", nullable = false, unique = true)
    private Funcionario funcionario;
}
