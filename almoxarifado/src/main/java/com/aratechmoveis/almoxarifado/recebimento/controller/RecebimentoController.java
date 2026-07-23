package com.aratechmoveis.almoxarifado.recebimento.controller;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.recebimento.service.RecebimentoService;
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
@RequestMapping("almoxarifado/recebimento")
@RequiredArgsConstructor
public class RecebimentoController {

    private final RecebimentoService recebimentoService;

    @GetMapping("/fila")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO', 'ADMIN')")
    public ResponseEntity<Response> buscarFila() {
        return ResponseEntity.status(HttpStatus.OK).body(recebimentoService.buscarFila());
    }

    @GetMapping("/historico")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO', 'ADMIN')")
    public ResponseEntity<Response> buscarHistorico() {
        return ResponseEntity.status(HttpStatus.OK).body(recebimentoService.buscarHistorico());
    }

    @PostMapping("/autorizar/{notaFiscal}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO', 'ADMIN')")
    public ResponseEntity<Response> autorizarRecebimento(@PathVariable String notaFiscal) {
        return ResponseEntity.status(HttpStatus.OK).body(recebimentoService.autorizarRecebimento(notaFiscal));
    }

    @PutMapping("/finalizar/{notaFiscal}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_ALMOXARIFADO', 'CONFERENTE_ALMOXARIFADO', 'ADMIN')")
    public ResponseEntity<Response> finalizarRecebimento(@PathVariable String notaFiscal) {
        return ResponseEntity.status(HttpStatus.OK).body(recebimentoService.finalizarRecebimento(notaFiscal));
    }
}