package com.aratechmoveis.recursoshumanos.funcionarios.controller;

import com.aratechmoveis.recursoshumanos.funcionarios.dto.AtribuirPerfisDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.dto.FuncionarioDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.dto.LoginFuncionarioDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.service.FuncionarioService;
import com.aratechmoveis.recursoshumanos.response.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("recursoshumanos/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @PostMapping("/adicionar")
    public ResponseEntity<Response> adicionarFuncionario(@RequestBody @Valid FuncionarioDTO funcionario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionarioService.adicionarFuncionario(funcionario));
    }

    @GetMapping("/todos")
    public ResponseEntity<Response> listarFuncionarios() {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.listarFuncionarios());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Response> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.buscarFuncionariosPorNome(nome));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Response> atualizarFuncionario(@PathVariable @Min(1) Long id, @RequestBody @Valid FuncionarioDTO funcionario) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.atualizarFuncionario(id, funcionario));
    }

    @PatchMapping("/ativar/{id}")
    public ResponseEntity<Response> ativarFuncionario(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.ativarFuncionario(id));
    }

    @PatchMapping("/desativar/{id}")
    public ResponseEntity<Response> desativarFuncionario(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.desativarFuncionario(id));
    }

    @PatchMapping("/perfis/{id}")
    public ResponseEntity<Response> atribuirPerfis(@PathVariable @Min(1) Long id, @RequestBody @Valid AtribuirPerfisDTO atribuirPerfisDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.atribuirPerfis(id, atribuirPerfisDTO));
    }

    @PostMapping("/email-corporativo")
    public ResponseEntity<Response> atribuirEmailCorporativo(@RequestBody LoginFuncionarioDTO loginFuncionarioDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(funcionarioService.atribuirEmailCorporativo(loginFuncionarioDTO));
    }
}
