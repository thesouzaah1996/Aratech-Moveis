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

    @PostMapping("/add")
    public ResponseEntity<Response> addCategoria(@RequestBody @Valid CategoriaDTO categoriaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criarCategoria(categoriaDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getCategorias() {
        return ResponseEntity.status(HttpStatus.OK).body(categoriaService.getCategorias());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateCategoria(@PathVariable @Min(1) Long id, @RequestBody @Valid CategoriaDTO categoriaDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(categoriaService.updateCategoria(id, categoriaDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deleteCategoria(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(categoriaService.deleteCategoria(id));
    }

    @GetMapping("/lookup-categoria")
    public ResponseEntity<Response> lookupCategoria() {
        return ResponseEntity.status(HttpStatus.OK).body(categoriaService.lookupCategoria());
    }
}
