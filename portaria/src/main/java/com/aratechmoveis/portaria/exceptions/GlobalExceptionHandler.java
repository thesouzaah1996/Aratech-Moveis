package com.aratechmoveis.portaria.exceptions;

import com.aratechmoveis.portaria.response.Response;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response> handleNotFound(NotFoundException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RecursoJaExistenteException.class)
    public ResponseEntity<Response> handleRecursoJaExistente(RecursoJaExistenteException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegistroNaoAutorizadoException.class)
    public ResponseEntity<Response> handleRegistroNaoAutorizado(RegistroNaoAutorizadoException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleValidacao(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(" "));
        return responder(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response> handleConstraintViolation(ConstraintViolationException e) {
        String mensagem = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(" "));
        return responder(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseEntity<Response> responder(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Response.builder()
                .status(status.value())
                .mensagem(mensagem)
                .build());
    }
}
