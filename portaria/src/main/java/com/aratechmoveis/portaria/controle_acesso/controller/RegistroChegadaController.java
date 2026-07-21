package com.aratechmoveis.portaria.controle_acesso.controller;

import com.aratechmoveis.portaria.response.Response;
import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;
import com.aratechmoveis.portaria.controle_acesso.entity.StatusCaminhao;
import com.aratechmoveis.portaria.controle_acesso.service.RegistroChegadaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("portaria/registro-chegada")
@RequiredArgsConstructor
public class RegistroChegadaController {

    private final RegistroChegadaService registroChegadaService;

    @PostMapping("/registrar")
    public ResponseEntity<Response> adicionarRegistroChegada(@RequestBody @Valid RegistroChegadaDTO registroChegadaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroChegadaService.adicionarRegistroChegada(registroChegadaDTO));
    }

    @GetMapping("/fila")
    public ResponseEntity<Response> buscarFila() {
        return ResponseEntity.status(HttpStatus.OK).body(registroChegadaService.buscarFila());
    }

    @GetMapping("/historico")
    public ResponseEntity<Response> buscarHistorico() {
        return ResponseEntity.status(HttpStatus.OK).body(registroChegadaService.buscarHistorico());
    }

    @GetMapping("/autorizados")
    public ResponseEntity<Response> listarAutorizados() {
        return ResponseEntity.status(HttpStatus.OK).body(registroChegadaService.listarAutorizados(StatusCaminhao.AUTORIZADO));
    }

    @PutMapping("/finalizar")
    public ResponseEntity<Response> finalizarRegistro(@RequestParam String notaFiscal) {
        return ResponseEntity.status(HttpStatus.OK).body(registroChegadaService.finalizarRegistro(notaFiscal));
    }
}
