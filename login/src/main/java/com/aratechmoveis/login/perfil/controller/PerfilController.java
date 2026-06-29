package com.aratechmoveis.login.perfil.controller;

import com.aratechmoveis.login.Response;
import com.aratechmoveis.login.perfil.dto.PerfilDTO;
import com.aratechmoveis.login.perfil.service.PerfilService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("login/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @PostMapping("/add")
    public ResponseEntity<Response> addPerfil(@RequestBody @Valid PerfilDTO perfilDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilService.criarPerfil(perfilDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getPerfis() {
        return ResponseEntity.status(HttpStatus.OK).body(perfilService.getPerfis());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updatePerfil(@PathVariable @Min(1) Long id, @RequestBody @Valid PerfilDTO perfilDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(perfilService.updatePerfil(id, perfilDTO));
    }

    @PatchMapping("/enable/{id}")
    public ResponseEntity<Response> ativarPerfil(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(perfilService.ativarPerfil(id));
    }

    @PatchMapping("/disable/{id}")
    public ResponseEntity<Response> desativarPerfil(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(perfilService.desativarPerfil(id));
    }
}
