package com.aratech.moveis.carregamento.expedicao.controller;

import com.aratech.moveis.carregamento.expedicao.service.ExpedicaoService;
import com.aratech.moveis.pcp.producao.dto.Ordem;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Carregamento — Expedição da produção",
        description =
                "Ordens integralmente concluídas aguardam expedição. Saída integral vinculada a"
                    + " caminhão CARREGAR com entrada confirmada pela Portaria (EM_RECEBIMENTO)."
                    + " Sem paletes, endereçamento ou bipagem. Não finaliza automaticamente a"
                    + " chegada: outras ordens podem compartilhar o mesmo caminhão. Registra data e"
                    + " usuário; não confirma entrega no cliente.")
@AuditadoNoServico
@RestController
@RequestMapping("/aratech/carregamento/expedicao")
@RequiredArgsConstructor
public class ExpedicaoController {
    private final ExpedicaoService service;

    @Operation(
            summary = "Consultar ordens prontas para carregar",
            description = "Retorna produção concluída ainda não expedida, com paginação e busca.")
    @GetMapping
    public Pagina<Ordem> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.ordens("expedicao", pagina, tamanho, busca);
    }

    @Operation(
            summary = "Consultar caminhões presentes para carregamento",
            description =
                    "Somente id, placa e empresa de chegadas CARREGAR em EM_RECEBIMENTO. Não expõe"
                            + " histórico completo da Portaria.")
    @GetMapping("/chegadas")
    public List<Opcao> chegadas() {
        return service.chegadas();
    }

    @Operation(
            summary = "Confirmar saída integral da ordem",
            description =
                    "Exige FINALIZAR, versão, UUID, motivo e referenciaId da chegada. Verifica"
                        + " ordem pronta e chegada ainda presente sob bloqueio. Marca expedição e"
                        + " atualiza venda vinculada para EXPEDIDO; repetição idempotente não"
                        + " duplica.")
    @PostMapping("/{id}/finalizar")
    public Ordem finalizar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.expedir(id, dto);
    }
}
