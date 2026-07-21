package com.aratechmoveis.almoxarifado.fornecedor.controller;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorDTO;
import com.aratechmoveis.almoxarifado.fornecedor.service.FornecedorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("almoxarifado/fornecedor")
@RequiredArgsConstructor
@CrossOrigin("*")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @PostMapping("/adicionar")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'ADMIN')")
    public ResponseEntity<Response> adicionarFornecedor(@RequestBody @Valid FornecedorDTO fornecedorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.adicionarFornecedor(fornecedorDTO));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO', 'ADMIN')")
    public ResponseEntity<Response> listarFornecedores() {
        return ResponseEntity.status(HttpStatus.OK).body(fornecedorService.listarFornecedores());
    }

    @PatchMapping("/desativar/{id}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'ADMIN')")
    public ResponseEntity<Response> desativarFornecedor(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(fornecedorService.desativarFornecedor(id));
    }

    @PatchMapping("/ativar/{id}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'ADMIN')")
    public ResponseEntity<Response> ativarFornecedor(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(fornecedorService.ativarFornecedor(id));
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'ADMIN')")
    public ResponseEntity<Response> atualizarFornecedor(@PathVariable @Min(1) Long id, @RequestBody FornecedorDTO fornecedorDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(fornecedorService.atualizarFornecedor(id, fornecedorDTO));
    }

    @GetMapping("/opcoes-fornecedor")
    public ResponseEntity<Response> buscarOpcoesFornecedor() {
        return ResponseEntity.status(HttpStatus.OK).body(fornecedorService.buscarOpcoesFornecedor());
    }
}
