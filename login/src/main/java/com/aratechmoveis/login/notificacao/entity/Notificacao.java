package com.aratechmoveis.login.notificacao.entity;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.notificacao.enums.TipoNotificacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "notificacoes", indexes = {
        @Index(name = "idx_notificacoes_funcionario", columnList = "funcionario_id"),
        @Index(name = "idx_notificacoes_data_criacao", columnList = "dataCriacao")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 255)
    private String assunto;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Column(length = 2000)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoNotificacao tipoNotificacao;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
