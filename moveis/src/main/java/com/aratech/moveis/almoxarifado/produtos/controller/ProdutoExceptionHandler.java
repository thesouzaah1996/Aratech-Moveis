package com.aratech.moveis.almoxarifado.produtos.controller;

import com.aratech.moveis.almoxarifado.produtos.dto.Response;
import com.aratech.moveis.almoxarifado.produtos.exception.ProdutoException;

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

@RestControllerAdvice(assignableTypes = ProdutoController.class)
@Slf4j
public class ProdutoExceptionHandler {
    @ExceptionHandler(ProdutoException.class)
    public ResponseEntity<Response> negocio(ProdutoException e) {
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
        log.warn("Operacao de produto rejeitada por restricao de integridade.", e);
        return resposta(
                HttpStatus.CONFLICT,
                "Confira se o SKU ou a chave da operacao ja esta em uso.",
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
        log.error("Falha ao executar operacao de produto.", e);
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
