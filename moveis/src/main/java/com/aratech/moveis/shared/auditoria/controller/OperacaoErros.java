package com.aratech.moveis.shared.auditoria.controller;

import com.aratech.moveis.almoxarifado.produtos.exception.ProdutoException;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice(annotations = AuditadoNoServico.class)
public class OperacaoErros {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> negocio(AuthException e) {
        return ResponseEntity.status(e.status())
                .body(Map.of("status", e.status(), "mensagem", e.getMessage()));
    }

    @ExceptionHandler(ProdutoException.class)
    public ResponseEntity<?> estoque(ProdutoException e) {
        return ResponseEntity.status(e.getStatus())
                .body(Map.of("status", e.getStatus().value(), "mensagem", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> campos(MethodArgumentNotValidException e) {
        var erros = new LinkedHashMap<String, String>();
        e.getBindingResult()
                .getFieldErrors()
                .forEach(f -> erros.put(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(
                        Map.of(
                                "status",
                                400,
                                "mensagem",
                                "Confira os campos informados.",
                                "erros",
                                erros));
    }

    @ExceptionHandler({
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class
    })
    public ResponseEntity<?> formato(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("mensagem", "Confira os formatos informados."));
    }

    @ExceptionHandler({
        org.springframework.dao.DataIntegrityViolationException.class,
        org.springframework.dao.ConcurrencyFailureException.class
    })
    public ResponseEntity<?> conflito(Exception e) {
        return ResponseEntity.status(409)
                .body(
                        Map.of(
                                "mensagem",
                                "Conflito de cadastro ou operação concorrente. Confira os dados e"
                                        + " tente novamente."));
    }
}
