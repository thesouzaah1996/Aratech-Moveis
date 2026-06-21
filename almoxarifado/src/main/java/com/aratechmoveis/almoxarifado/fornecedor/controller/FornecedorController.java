package com.aratechmoveis.almoxarifado.fornecedor.controller;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorDTO;
import com.aratechmoveis.almoxarifado.fornecedor.service.FornecedorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("almoxarifado/fornecedor")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @PostMapping("/add")
//    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addFornecedor(@RequestBody @Valid FornecedorDTO fornecedorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.addFornecedor(fornecedorDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllFornecedores() {
        return ResponseEntity.status(HttpStatus.OK).body(fornecedorService.getFornecedores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getFornecedorById(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(fornecedorService.getFornecedorById(id));
    }

    @PostMapping("/disable/{id}")
    public ResponseEntity<Response> disableFornecedor(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(fornecedorService.disableFornecedor(id));
    }
}
