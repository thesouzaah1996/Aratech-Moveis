package com.aratech.moveis.assistencia.estoque.controller;

import com.aratech.moveis.assistencia.estoque.dto.Response;
import com.aratech.moveis.assistencia.estoque.exception.PecaAssistenciaException;

import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = PecaAssistenciaController.class)
@Slf4j
public class PecaAssistenciaExceptionHandler {
    @ExceptionHandler(PecaAssistenciaException.class)
    public ResponseEntity<Response> negocio(PecaAssistenciaException e) {
        return resposta(e.getStatus(), e.getMessage(), null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response> validacao(ConstraintViolationException e) {
        Map<String, String> erros = new LinkedHashMap<>();
        e.getConstraintViolations()
                .forEach(
                        erro ->
                                erros.putIfAbsent(
                                        erro.getPropertyPath().toString(), erro.getMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Confira os campos informados.", erros);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response> integridade(DataIntegrityViolationException e) {
        log.warn(
                "Operacao de estoque de assistencia tecnica rejeitada por restricao de"
                        + " integridade.",
                e);
        return resposta(
                HttpStatus.CONFLICT,
                "Confira se o codigo ou a chave da operacao ja esta em uso.",
                null);
    }

    @ExceptionHandler({
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Response> formatoInvalido(Exception e) {
        return resposta(
                HttpStatus.BAD_REQUEST, "Confira o formato e os dados da requisicao.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> inesperado(Exception e) {
        if (e instanceof ErrorResponse erro && erro.getStatusCode().is4xxClientError()) {
            return ResponseEntity.status(erro.getStatusCode())
                    .body(
                            Response.builder()
                                    .status(erro.getStatusCode().value())
                                    .mensagem("Confira o formato e os dados da requisicao.")
                                    .build());
        }
        log.error("Falha ao executar operacao de estoque de assistencia tecnica.", e);
        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Nao foi possivel concluir a operacao. Tente novamente.",
                null);
    }

    private ResponseEntity<Response> resposta(
            HttpStatus status, String mensagem, Map<String, String> erros) {
        return ResponseEntity.status(status)
                .body(
                        Response.builder()
                                .status(status.value())
                                .mensagem(mensagem)
                                .erros(erros)
                                .build());
    }
}
