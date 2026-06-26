package com.aratechmoveis.almoxarifado.produto.controller;

import com.aratechmoveis.almoxarifado.res.Response;
import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;
import com.aratechmoveis.almoxarifado.produto.service.ProdutoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("almoxarifado/produto")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping("/add")
    public ResponseEntity<Response> addProduto(@RequestBody @Valid ProdutoDTO produtoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.addProduto(produtoDTO));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateProduto(@PathVariable @Min(1) Long id, @RequestBody ProdutoDTO produtoDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(produtoService.updateProduto(id, produtoDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getProdutos() {
        return ResponseEntity.status(HttpStatus.OK).body(produtoService.getProdutos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getProdutoById(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(produtoService.getProdutoById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deleteProduto(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(produtoService.deleteProduto(id));
    }
}
