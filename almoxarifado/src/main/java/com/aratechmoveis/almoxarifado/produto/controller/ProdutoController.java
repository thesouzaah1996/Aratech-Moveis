package com.aratechmoveis.almoxarifado.produto.controller;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;
import com.aratechmoveis.almoxarifado.produto.service.ProdutoService;
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
@RequestMapping("almoxarifado/produto")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping("/adicionar")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO')")
    public ResponseEntity<Response> adicionarProduto(@RequestBody @Valid ProdutoDTO produtoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.adicionarProduto(produtoDTO));
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO')")
    public ResponseEntity<Response> atualizarProduto(@PathVariable @Min(1) Long id, @RequestBody ProdutoDTO produtoDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(produtoService.atualizarProduto(id, produtoDTO));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO', 'COMPRAS', 'ENCARREGADO_PINTURA', 'ENCARREGADO_EMBALAGEM', 'ENCARREGADO_BORDA', 'GERENTE_GERAL')")
    public ResponseEntity<Response> listarProdutos() {
        return ResponseEntity.status(HttpStatus.OK).body(produtoService.listarProdutos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO', 'COMPRAS', 'ENCARREGADO_PINTURA', 'ENCARREGADO_EMBALAGEM', 'ENCARREGADO_BORDA', 'GERENTE_GERAL')")
    public ResponseEntity<Response> buscarProdutoPorId(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(produtoService.buscarProdutoPorId(id));
    }

    @DeleteMapping("/remover/{id}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO')")
    public ResponseEntity<Response> removerProduto(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(produtoService.removerProduto(id));
    }
}
