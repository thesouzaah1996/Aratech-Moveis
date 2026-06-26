package com.aratechmoveis.almoxarifado.exceptions;

import com.aratechmoveis.almoxarifado.res.Response;
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
    public ResponseEntity<Response> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Response.builder().status(404).mensagem(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(RecursoJaExistenteException.class)
    public ResponseEntity<Response> handleConflict(RecursoJaExistenteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Response.builder().status(409).mensagem(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Response.builder().status(400).mensagem(msg).build()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response> handleConstraint(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Response.builder().status(400).mensagem(msg).build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Response.builder().status(500).mensagem("Erro interno: " + ex.getMessage()).build()
        );
    }
}
