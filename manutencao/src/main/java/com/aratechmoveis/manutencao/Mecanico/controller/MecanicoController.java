package com.aratechmoveis.manutencao.Mecanico.controller;

import com.aratechmoveis.manutencao.Mecanico.dto.MecanicoDTO;
import com.aratechmoveis.manutencao.Mecanico.service.MecanicoService;
import com.aratechmoveis.manutencao.Response;
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
@RequestMapping("manutencao/mecanico")
@RequiredArgsConstructor
public class MecanicoController {

    private final MecanicoService mecanicoService;

    @PostMapping("/adicionar")
    @PreAuthorize("hasAnyRole('ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> adicionarMecanico(@RequestBody @Valid MecanicoDTO mecanicoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mecanicoService.adicionarMecanico(mecanicoDTO));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('CONFERENTE_MANUTENCAO', 'ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> listarMecanicos() {
        return ResponseEntity.status(HttpStatus.OK).body(mecanicoService.listarMecanicos());
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> atualizarMecanico(@PathVariable @Min(1) Long id, @RequestBody @Valid MecanicoDTO mecanicoDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(mecanicoService.atualizarMecanico(id, mecanicoDTO));
    }

    @PatchMapping("/desativar/{id}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> desativarMecanico(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(mecanicoService.desativarMecanico(id));
    }

    @PatchMapping("/ativar/{id}")
    @PreAuthorize("hasAnyRole('ENCARREGADO_MANUTENCAO', 'ADMIN')")
    public ResponseEntity<Response> ativarMecanico(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(mecanicoService.ativarMecanico(id));
    }

    @GetMapping("/opcoes-mecanico")
    public ResponseEntity<Response> buscarOpcoesMecanico() {
        return ResponseEntity.status(HttpStatus.OK).body(mecanicoService.buscarOpcoesMecanico());
    }
}
