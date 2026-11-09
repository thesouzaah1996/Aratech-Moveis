package com.aratech.moveis.suporte.chamados.controller;

import com.aratech.moveis.suporte.chamados.dto.ChamadoTiDTO;
import com.aratech.moveis.suporte.chamados.dto.Response;
import com.aratech.moveis.suporte.chamados.service.ChamadoTiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Suporte — Chamados de TI",
        description =
                "Criação valida tipo, prioridade e módulo afetado. Título e solicitante têm 3 a 150"
                    + " caracteres; setor, 2 a 100; descrição, 3 a 3000; tentativas de solução, até"
                    + " 2000. Contato opcional: ramal de 2 a 6 dígitos ou telefone com DDD de 10/11"
                    + " dígitos. chaveOperacao é obrigatória e idempotente. Não há endpoint de"
                    + " conclusão de TI implementado neste controller.")
@RestController
@RequestMapping("/aratech/suporte/chamados")
@RequiredArgsConstructor
public class ChamadoTiController {
    private final ChamadoTiService service;

    @Operation(
            summary = "Cadastrar chamado de TI",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> abrir(@RequestBody ChamadoTiDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @Operation(
            summary = "Listar chamados de TI",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca) {
        return service.listar(pagina, tamanho, status, busca);
    }

    @Operation(
            summary = "Consultar chamado de TI por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
