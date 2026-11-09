package com.aratech.moveis.portaria.controleacesso.controller;

import com.aratech.moveis.almoxarifado.recebimento.controller.RecebimentoController;
import com.aratech.moveis.carregamento.controller.CarregamentoController;
import com.aratech.moveis.portaria.controleacesso.dto.Erro;
import com.aratech.moveis.portaria.controleacesso.exception.ControleAcessoException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(
        assignableTypes = {
            ControleAcessoController.class,
            RecebimentoController.class,
            CarregamentoController.class
        })
@Slf4j
public class ControleAcessoExceptionHandler {

    @ExceptionHandler(ControleAcessoException.class)
    public ResponseEntity<Erro> negocio(ControleAcessoException e) {
        return erro(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> duplicidade(DataIntegrityViolationException e) {
        return erro(
                HttpStatus.CONFLICT,
                "Chegada duplicada ou dados inconsistentes. Atualize a fila antes de tentar"
                        + " novamente.");
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Erro> formato(Exception e) {
        return erro(HttpStatus.BAD_REQUEST, "Confira o formato dos dados da requisicao.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Erro> inesperado(Exception e) {
        if (e instanceof org.springframework.web.ErrorResponse r
                && r.getStatusCode().is4xxClientError())
            return ResponseEntity.status(r.getStatusCode())
                    .body(new Erro(r.getStatusCode().value(), "Confira os dados da requisicao."));
        log.error("Falha no controle de chegadas.", e);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Nao foi possivel concluir a operacao.");
    }

    private ResponseEntity<Erro> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new Erro(status.value(), mensagem));
    }
}
