package com.aratech.moveis.manutencao.chamados.controller;

import com.aratech.moveis.manutencao.chamados.dto.ChamadoDTO;
import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.chamados.service.ChamadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Manutenção — Chamados",
        description =
                "Solicitação valida equipamento, setor, solicitante, telefone, descrição, tipo e"
                    + " prioridade. chaveOperacao é obrigatória e idempotente: repetir os mesmos"
                    + " dados não duplica; reutilizar com outro conteúdo gera conflito. A"
                    + " atribuição e a conclusão são realizadas na fila de manutenção.")
@RestController
@RequestMapping("/aratech/manutencao/chamados")
@RequiredArgsConstructor
public class ChamadoController {
    private final ChamadoService service;

    @Operation(
            summary = "Cadastrar chamado de manutenção",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> solicitar(@RequestBody ChamadoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitar(dto));
    }

    @Operation(
            summary = "Listar chamados de manutenção",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(required = false) String status) {
        return service.listar(pagina, tamanho, status);
    }

    @Operation(
            summary = "Consultar chamado de manutenção por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
