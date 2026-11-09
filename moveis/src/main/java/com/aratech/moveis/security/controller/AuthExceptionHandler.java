package com.aratech.moveis.security.controller;

import com.aratech.moveis.compras.pedidos.controller.PedidoCompraController;
import com.aratech.moveis.compras.pedidos.controller.PedidosOrigemController;
import com.aratech.moveis.rh.funcionarios.controller.FuncionarioController;
import com.aratech.moveis.rh.perfis.controller.AtribuicaoPerfilController;
import com.aratech.moveis.rh.ponto.controller.PontoController;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.security.exception.ExcessoTentativas;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice(
        assignableTypes = {
            AuthController.class,
            PedidoCompraController.class,
            PedidosOrigemController.class,
            PerfilUsuarioController.class,
            PontoController.class,
            GestaoAcessoController.class,
            AtribuicaoPerfilController.class,
            FuncionarioController.class
        })
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> negocio(AuthException e) {
        return ResponseEntity.status(e.status()).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(ExcessoTentativas.class)
    public ResponseEntity<?> limite(Exception e) {
        return ResponseEntity.status(429)
                .header("Retry-After", "900")
                .body(Map.of("mensagem", "Aguarde e tente novamente."));
    }

    @ExceptionHandler({
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class
    })
    public ResponseEntity<?> formato(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("mensagem", "Confira os dados informados."));
    }
}
