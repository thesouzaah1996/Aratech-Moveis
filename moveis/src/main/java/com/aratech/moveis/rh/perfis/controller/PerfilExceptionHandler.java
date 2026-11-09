package com.aratech.moveis.rh.perfis.controller;

import com.aratech.moveis.rh.perfis.dto.Erro;
import com.aratech.moveis.rh.perfis.exception.PerfilException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = {PerfilController.class, AtribuicaoPerfilController.class})
public class PerfilExceptionHandler {

    @ExceptionHandler(PerfilException.class)
    public ResponseEntity<Erro> negocio(PerfilException e) {
        return erro(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Erro> formato(Exception e) {
        return erro(HttpStatus.BAD_REQUEST, "Formato de requisicao invalido.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> duplicidade(DataIntegrityViolationException e) {
        return erro(HttpStatus.CONFLICT, "Perfil ja cadastrado ou dados inconsistentes.");
    }

    private ResponseEntity<Erro> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new Erro(status.value(), mensagem));
    }
}
