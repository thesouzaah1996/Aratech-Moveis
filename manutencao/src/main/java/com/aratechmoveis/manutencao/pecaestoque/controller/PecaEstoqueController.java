package com.aratechmoveis.manutencao.pecaestoque.controller;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.pecaestoque.dto.PecaEstoqueDTO;
import com.aratechmoveis.manutencao.pecaestoque.service.PecaEstoqueService;
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
@RequestMapping("manutencao/peca-estoque")
@RequiredArgsConstructor
public class PecaEstoqueController {

    private final PecaEstoqueService pecaEstoqueService;

    @PostMapping("/adicionar")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> adicionarPecaEstoque(@RequestBody @Valid PecaEstoqueDTO pecaEstoqueDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pecaEstoqueService.adicionarPecaEstoque(pecaEstoqueDTO));
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> atualizarPecaEstoque(@PathVariable @Min(1) Long id, @RequestBody PecaEstoqueDTO pecaEstoqueDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(pecaEstoqueService.atualizarPecaEstoque(id, pecaEstoqueDTO));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> listarPecasEstoque() {
        return ResponseEntity.status(HttpStatus.OK).body(pecaEstoqueService.listarPecasEstoque());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> buscarPecaEstoquePorId(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(pecaEstoqueService.buscarPecaEstoquePorId(id));
    }

    @DeleteMapping("/remover/{id}")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> removerPecaEstoque(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(pecaEstoqueService.removerPecaEstoque(id));
    }
}
