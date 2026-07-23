package com.aratechmoveis.login.exceptions;

import com.aratechmoveis.login.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Response> handleUnauthorized(UnauthorizedException e) {
        HttpStatus status = e.isBloqueado() ? HttpStatus.LOCKED : HttpStatus.UNAUTHORIZED;

        return ResponseEntity.status(status).body(Response.builder()
                .status(status.value())
                .mensagem(e.getMessage())
                .build());
    }
}
