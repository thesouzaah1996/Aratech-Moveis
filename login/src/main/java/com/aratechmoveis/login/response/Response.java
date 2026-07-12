package com.aratechmoveis.login.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private int status;
    private String mensagem;

    private String token;
    private String expirationTime;

    private T dados;

    private final LocalDateTime timestamp = LocalDateTime.now();
}
