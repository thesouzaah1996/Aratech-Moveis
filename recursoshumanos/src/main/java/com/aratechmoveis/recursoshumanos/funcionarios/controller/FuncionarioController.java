package com.aratechmoveis.recursoshumanos.funcionarios.controller;

import com.aratechmoveis.recursoshumanos.funcionarios.dto.FuncionarioDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.service.FuncionarioService;
import com.aratechmoveis.recursoshumanos.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("recursoshumanos/funcionario")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @PostMapping("/add")
    public ResponseEntity<Response> addFuncionario(@RequestBody @Valid FuncionarioDTO funcionario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionarioService.addFuncionario(funcionario));
    }

    @GetMapping("/find-nome")
    public ResponseEntity<Response> getFuncionarioByNome(@RequestBody String nome) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.getFuncionarioByNome(nome));
    }
}
