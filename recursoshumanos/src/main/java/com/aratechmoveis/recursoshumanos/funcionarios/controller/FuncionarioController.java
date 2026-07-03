package com.aratechmoveis.recursoshumanos.funcionarios.controller;

import com.aratechmoveis.recursoshumanos.funcionarios.dto.AtribuirPerfisDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.dto.FuncionarioDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.service.FuncionarioService;
import com.aratechmoveis.recursoshumanos.response.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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

    @GetMapping("/all")
    public ResponseEntity<Response> getFuncionarios() {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.getFuncionarios());
    }

    @GetMapping("/search")
    public ResponseEntity<Response> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.getFuncionariosPorNome(nome));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateFuncionario(@PathVariable Long id, @RequestBody @Valid FuncionarioDTO funcionario) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.updateFuncionario(id, funcionario));
    }

    @PatchMapping("/enable/{id}")
    public ResponseEntity<Response> enableFuncionario(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.enableFuncionario(id));
    }

    @PatchMapping("/disable/{id}")
    public ResponseEntity<Response> disableFuncionario(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.disableFuncionario(id));
    }

    @PatchMapping("/perfis/{id}")
    public ResponseEntity<Response> atribuirPerfis(@PathVariable @Min(1) Long id, @RequestBody @Valid AtribuirPerfisDTO atribuirPerfisDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.atribuirPerfis(id, atribuirPerfisDTO));
    }
}
