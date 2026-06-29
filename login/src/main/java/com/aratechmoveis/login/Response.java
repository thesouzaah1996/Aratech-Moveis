package com.aratechmoveis.login;

import com.aratechmoveis.login.perfil.dto.PerfilDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private int status;
    private String message;

    private String token;
    private String expirationTime;

    private PerfilDTO perfil;
    private List<PerfilDTO> perfis;

    private final LocalDateTime timestamp = LocalDateTime.now();
}
