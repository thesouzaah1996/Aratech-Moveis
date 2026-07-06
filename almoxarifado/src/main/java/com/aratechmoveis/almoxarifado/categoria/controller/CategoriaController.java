package com.aratechmoveis.almoxarifado.categoria.controller;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaDTO;
import com.aratechmoveis.almoxarifado.categoria.service.CategoriaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("almoxarifado/categoria")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping("/adicionar")
    public ResponseEntity<Response> adicionarCategoria(@RequestBody @Valid CategoriaDTO categoriaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criarCategoria(categoriaDTO));
    }

    @GetMapping("/todos")
    public ResponseEntity<Response> listarCategorias() {
        return ResponseEntity.status(HttpStatus.OK).body(categoriaService.listarCategorias());
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Response> atualizarCategoria(@PathVariable @Min(1) Long id, @RequestBody @Valid CategoriaDTO categoriaDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(categoriaService.atualizarCategoria(id, categoriaDTO));
    }

    @DeleteMapping("/remover/{id}")
    public ResponseEntity<Response> removerCategoria(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(categoriaService.removerCategoria(id));
    }

    @GetMapping("/opcoes-categoria")
    public ResponseEntity<Response> buscarOpcoesCategoria() {
        return ResponseEntity.status(HttpStatus.OK).body(categoriaService.buscarOpcoesCategoria());
    }
}
