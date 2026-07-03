package com.aratechmoveis.portaria.controle_acesso.controller;

import com.aratechmoveis.portaria.response.Response;
import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;
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

    @PostMapping("/add")
    public ResponseEntity<Response> addRegistroChegada(@RequestBody @Valid RegistroChegadaDTO registroChegadaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroChegadaService.addRegistroChegada(registroChegadaDTO));
    }

    @GetMapping("/fila")
    public ResponseEntity<Response> getFila() {
        return ResponseEntity.status(HttpStatus.OK).body(registroChegadaService.getFila());
    }

    @GetMapping("/historico")
    public ResponseEntity<Response> getHistorico() {
        return ResponseEntity.status(HttpStatus.OK).body(registroChegadaService.getHistorico());
    }

    @PutMapping("/finalizar/{id}")
    public ResponseEntity<Response> finalizarRegistro(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(registroChegadaService.finalizarRegistro(id));
    }
}
