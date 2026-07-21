package com.aratechmoveis.login.notificacao.dto;

import com.aratechmoveis.login.notificacao.enums.TipoNotificacao;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NotificacaoDTO {

    @EqualsAndHashCode.Include
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Size(max = 255, message = "O assunto deve ter no máximo 255 caracteres.")
    private String assunto;

    @NotBlank(message = "Destinatario é obrigatório")
    @Email(message = "Destinatário inválido.")
    @Size(max = 150, message = "O destinatário deve ter no máximo 150 caracteres.")
    private String destinatario;

    @Size(max = 2000, message = "A mensagem deve ter no máximo 2000 caracteres.")
    private String mensagem;

    private TipoNotificacao tipoNotificacao;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataCriacao;

    private String nomeTemplate;

    @ToString.Exclude
    private Map<String, Object> variaveisTemplate;

    private String logoPath;
}
