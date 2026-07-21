package com.aratechmoveis.manutencao.solicitacaopeca.controller;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.solicitacaopeca.dto.SolicitacaoPecaDTO;
import com.aratechmoveis.manutencao.solicitacaopeca.service.SolicitacaoPecaService;
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
@RequestMapping("manutencao/solicitacao-peca")
@RequiredArgsConstructor
public class SolicitacaoPecaController {

    private final SolicitacaoPecaService solicitacaoPecaService;

    @PostMapping("/adicionar")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ENCARREGADO_USINAGEM', 'ADMIN')")
    public ResponseEntity<Response> adicionarSolicitacaoPeca(@RequestBody @Valid SolicitacaoPecaDTO solicitacaoPecaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoPecaService.adicionarSolicitacaoPeca(solicitacaoPecaDTO));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ENCARREGADO_USINAGEM', 'ADMIN')")
    public ResponseEntity<Response> listarSolicitacoesPeca() {
        return ResponseEntity.status(HttpStatus.OK).body(solicitacaoPecaService.listarSolicitacoesPeca());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ENCARREGADO_USINAGEM', 'ADMIN')")
    public ResponseEntity<Response> buscarSolicitacaoPecaPorId(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(solicitacaoPecaService.buscarSolicitacaoPecaPorId(id));
    }

    @DeleteMapping("/remover/{id}")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ENCARREGADO_USINAGEM', 'ADMIN')")
    public ResponseEntity<Response> removerSolicitacaoPeca(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(solicitacaoPecaService.removerSolicitacaoPeca(id));
    }
}
