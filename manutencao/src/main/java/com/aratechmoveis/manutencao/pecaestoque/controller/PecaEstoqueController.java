package com.aratechmoveis.manutencao.pecaestoque.controller;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.pecaestoque.dto.PecaEstoqueDTO;
import com.aratechmoveis.manutencao.pecaestoque.service.PecaEstoqueService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("manutencao/peca-estoque")
@RequiredArgsConstructor
public class PecaEstoqueController {

    private final PecaEstoqueService pecaEstoqueService;

    @PostMapping("/add")
    public ResponseEntity<Response> addPecaEstoque(@RequestBody @Valid PecaEstoqueDTO pecaEstoqueDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pecaEstoqueService.addPecaEstoque(pecaEstoqueDTO));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updatePecaEstoque(@PathVariable @Min(1) Long id, @RequestBody PecaEstoqueDTO pecaEstoqueDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(pecaEstoqueService.updatePecaEstoque(id, pecaEstoqueDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getPecasEstoque() {
        return ResponseEntity.status(HttpStatus.OK).body(pecaEstoqueService.getPecasEstoque());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getPecaEstoqueById(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(pecaEstoqueService.getPecaEstoqueById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deletePecaEstoque(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(pecaEstoqueService.deletePecaEstoque(id));
    }
}
