package com.aratech.moveis.rh.funcionarios.controller;

import com.aratech.moveis.rh.funcionarios.dto.Response;
import com.aratech.moveis.rh.funcionarios.exception.CadastroFuncionarioException;

import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = FuncionarioController.class)
@Slf4j
public class FuncionarioExceptionHandler {

    @ExceptionHandler(CadastroFuncionarioException.class)
    public ResponseEntity<Response> cadastro(CadastroFuncionarioException e) {
        return resposta(e.getStatus(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> validacao(MethodArgumentNotValidException e) {
        Map<String, String> erros = new LinkedHashMap<>();
        e.getBindingResult()
                .getFieldErrors()
                .forEach(
                        erro ->
                                erros.putIfAbsent(
                                        erro.getField(),
                                        erro.isBindingFailure()
                                                ? "Informe um valor válido."
                                                : erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Confira os campos informados.", erros);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response> restricao(ConstraintViolationException e) {
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
        log.warn("Cadastro de funcionário rejeitado por restrição de integridade.", e);
        return resposta(
                HttpStatus.CONFLICT,
                "Não foi possível cadastrar. Confira se CPF, e-mail ou PIS/NIT/NIS já estão em"
                        + " uso.",
                null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Response> tamanho(MaxUploadSizeExceededException e) {
        return resposta(
                HttpStatus.PAYLOAD_TOO_LARGE, "Os arquivos ultrapassam o tamanho permitido.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> inesperado(Exception e) {
        if (e instanceof ErrorResponse erro && erro.getStatusCode().is4xxClientError()) {
            return ResponseEntity.status(erro.getStatusCode())
                    .body(
                            Response.builder()
                                    .status(erro.getStatusCode().value())
                                    .mensagem("Confira o formato e os dados da requisição.")
                                    .build());
        }
        log.error("Falha ao cadastrar funcionário.", e);
        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível concluir o cadastro. Tente novamente.",
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
