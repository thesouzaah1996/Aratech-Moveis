package com.aratech.moveis.suporte.chamados.controller;

import com.aratech.moveis.suporte.chamados.dto.Response;
import com.aratech.moveis.suporte.chamados.exception.ChamadoTiException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = ChamadoTiController.class)
@Slf4j
public class ChamadoTiExceptionHandler {
    @ExceptionHandler(ChamadoTiException.class)
    public ResponseEntity<Response> negocio(ChamadoTiException e) {
        return erro(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response> integridade(DataIntegrityViolationException e) {
        return erro(
                HttpStatus.CONFLICT,
                "Solicitacao ja registrada ou dados inconsistentes. Confira os dados antes de"
                        + " tentar novamente.");
    }

    @ExceptionHandler({
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Response> formato(Exception e) {
        return erro(HttpStatus.BAD_REQUEST, "Confira o formato dos dados da requisicao.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> inesperado(Exception e) {
        if (e instanceof ErrorResponse erro && erro.getStatusCode().is4xxClientError())
            return ResponseEntity.status(erro.getStatusCode())
                    .body(
                            Response.builder()
                                    .status(erro.getStatusCode().value())
                                    .mensagem("Confira os dados da requisicao.")
                                    .build());
        log.error("Falha ao executar operacao de suporte TI.", e);
        return erro(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Nao foi possivel concluir a operacao. Tente novamente.");
    }

    private ResponseEntity<Response> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(Response.builder().status(status.value()).mensagem(mensagem).build());
    }
}
