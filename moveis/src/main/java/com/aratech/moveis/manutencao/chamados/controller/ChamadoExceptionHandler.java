package com.aratech.moveis.manutencao.chamados.controller;

import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.fila.controller.FilaChamadosController;
import com.aratech.moveis.manutencao.mecanicos.controller.MecanicoController;
import com.aratech.moveis.manutencao.pecas.controller.SolicitacaoPecaController;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(
        assignableTypes = {
            ChamadoController.class,
            SolicitacaoPecaController.class,
            FilaChamadosController.class,
            MecanicoController.class
        })
@Slf4j
public class ChamadoExceptionHandler {
    @ExceptionHandler(ChamadoException.class)
    public ResponseEntity<Response> negocio(ChamadoException e) {
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
        log.error("Falha ao executar operacao de manutencao.", e);
        return erro(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Nao foi possivel concluir a operacao. Tente novamente.");
    }

    private ResponseEntity<Response> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(Response.builder().status(status.value()).mensagem(mensagem).build());
    }
}
