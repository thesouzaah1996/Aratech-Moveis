package com.aratechmoveis.recursoshumanos.perfil.controller;


import com.aratechmoveis.recursoshumanos.perfil.dto.PerfilDTO;
import com.aratechmoveis.recursoshumanos.perfil.service.PerfilService;
import com.aratechmoveis.recursoshumanos.response.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("recursoshumanos/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @PostMapping("/adicionar")
    public ResponseEntity<Response> adicionarPerfil(@RequestBody @Valid PerfilDTO perfilDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilService.criarPerfil(perfilDTO));
    }

    @GetMapping("/todos")
    public ResponseEntity<Response> listarPerfis() {
        return ResponseEntity.status(HttpStatus.OK).body(perfilService.listarPerfis());
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Response> atualizarPerfil(@PathVariable @Min(1) Long id, @RequestBody @Valid PerfilDTO perfilDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(perfilService.atualizarPerfil(id, perfilDTO));
    }

    @PatchMapping("/ativar/{id}")
    public ResponseEntity<Response> ativarPerfil(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(perfilService.ativarPerfil(id));
    }

    @PatchMapping("/desativar/{id}")
    public ResponseEntity<Response> desativarPerfil(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(perfilService.desativarPerfil(id));
    }
}
