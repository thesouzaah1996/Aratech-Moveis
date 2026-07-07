package com.aratechmoveis.login.notificacao.entity;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.notificacao.enums.TipoNotificacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "notificacao")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assunto;

    private String destinatario;

    private String mensagem;

    @Enumerated(EnumType.STRING)
    private TipoNotificacao tipoNotificacao;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    private LocalDateTime dataCriacao;
}
