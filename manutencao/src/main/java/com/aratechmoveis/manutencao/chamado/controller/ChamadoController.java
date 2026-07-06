package com.aratechmoveis.manutencao.chamado.controller;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.chamado.dto.AtribuirMecanicoDTO;
import com.aratechmoveis.manutencao.chamado.dto.ChamadoDTO;
import com.aratechmoveis.manutencao.chamado.entity.StatusChamado;
import com.aratechmoveis.manutencao.chamado.service.ChamadoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("manutencao/chamado")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    @PostMapping("/adicionar")
    public ResponseEntity<Response> adicionarChamado(@RequestBody @Valid ChamadoDTO chamadoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.adicionarChamado(chamadoDTO));
    }

    @GetMapping("/todos")
    public ResponseEntity<Response> listarChamados(@RequestParam(required = false) StatusChamado status) {
        return ResponseEntity.status(HttpStatus.OK).body(chamadoService.listarChamados(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> buscarChamadoPorId(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(chamadoService.buscarChamadoPorId(id));
    }

    @PutMapping("/atribuir/{id}")
    public ResponseEntity<Response> atribuirMecanico(@PathVariable @Min(1) Long id,
                                                       @RequestBody @Valid AtribuirMecanicoDTO atribuirMecanicoDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(chamadoService.atribuirMecanico(id, atribuirMecanicoDTO));
    }

    @PutMapping("/concluir/{id}")
    public ResponseEntity<Response> concluirChamado(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(chamadoService.concluirChamado(id));
    }

    @DeleteMapping("/remover/{id}")
    public ResponseEntity<Response> removerChamado(@PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(chamadoService.removerChamado(id));
    }
}
