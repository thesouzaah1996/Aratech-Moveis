package com.aratech.moveis.manutencao.pecas.controller;

import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaDTO;
import com.aratech.moveis.manutencao.pecas.service.SolicitacaoPecaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Manutenção — Solicitações de peças",
        description =
                "Solicitação integra a fila de manutenção com origem PECA. Informe nome/código da"
                    + " peça, quantidade, unidade, equipamento, finalidade, prioridade e"
                    + " solicitante. A chaveOperacao evita duplicações e não pode ser reutilizada"
                    + " com outro conteúdo. Consulta por ID aceita somente registros de origem"
                    + " PECA. Na mesma transação, cria pedido de compra de origem MANUTENCAO já"
                    + " aguardando aprovação, sem depender do mínimo de estoque. Repetir a mesma"
                    + " chaveOperacao não duplica o chamado nem o pedido. Não baixa estoque"
                    + " automaticamente.")
@RestController
@RequestMapping("/aratech/manutencao/solicitacoes-pecas")
@RequiredArgsConstructor
public class SolicitacaoPecaController {
    private final SolicitacaoPecaService service;

    @Operation(
            summary = "Cadastrar solicitação de peça",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> solicitar(@RequestBody SolicitacaoPecaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitar(dto));
    }

    @Operation(
            summary = "Consultar solicitação de peça por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
