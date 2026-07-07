package com.aratechmoveis.login.notificacao.dto;

import com.aratechmoveis.login.notificacao.enums.TipoNotificacao;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoDTO {

    private Long id;

    private String assunto;

    @NotBlank(message = "Destinatario é obrigatório")
    private String destinatario;

    private String mensagem;

    private TipoNotificacao tipoNotificacao;

    private LocalDateTime dataCriacao;

    private String nomeTemplate;

    private Map<String, Object> variaveisTemplate;
}
