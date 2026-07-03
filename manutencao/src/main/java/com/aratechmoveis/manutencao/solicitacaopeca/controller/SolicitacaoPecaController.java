package com.aratechmoveis.manutencao.solicitacaopeca.controller;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.solicitacaopeca.dto.SolicitacaoPecaDTO;
import com.aratechmoveis.manutencao.solicitacaopeca.service.SolicitacaoPecaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("manutencao/solicitacao-peca")
@RequiredArgsConstructor
public class SolicitacaoPecaController {

    private final SolicitacaoPecaService solicitacaoPecaService;

    @PostMapping("/add")
    public ResponseEntity<Response> addSolicitacaoPeca(@RequestBody @Valid SolicitacaoPecaDTO solicitacaoPecaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoPecaService.addSolicitacaoPeca(solicitacaoPecaDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getSolicitacoesPeca() {
        return ResponseEntity.status(HttpStatus.OK).body(solicitacaoPecaService.getSolicitacoesPeca());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getSolicitacaoPecaById(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(solicitacaoPecaService.getSolicitacaoPecaById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deleteSolicitacaoPeca(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(solicitacaoPecaService.deleteSolicitacaoPeca(id));
    }
}
