package com.aratech.moveis.assistencia.lotes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice(assignableTypes = LoteAssistenciaController.class)
public class LoteAssistenciaErros {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> regra(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(
                        Map.of(
                                "mensagem",
                                e.getReason() == null
                                        ? "Operação incompatível com o lote."
                                        : e.getReason()));
    }

    @ExceptionHandler({
        org.springframework.dao.DataIntegrityViolationException.class,
        org.springframework.dao.ConcurrencyFailureException.class
    })
    public ResponseEntity<?> concorrencia(Exception e) {
        return ResponseEntity.status(409)
                .body(
                        Map.of(
                                "mensagem",
                                "Lote já registrado ou atualizado por outra operação. Confira os"
                                        + " dados antes de repetir."));
    }
}
