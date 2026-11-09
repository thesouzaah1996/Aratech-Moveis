package com.aratech.moveis.compras.cotacoes.controller;

import com.aratech.moveis.compras.cotacoes.dto.Cotacao;
import com.aratech.moveis.compras.cotacoes.dto.NovaCotacao;
import com.aratech.moveis.compras.cotacoes.service.CotacaoService;
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
        name = "Compras — Cotações",
        description =
                "Uma cotação aberta por pedido em rascunho/aguardando aprovação; até três"
                        + " fornecedores distintos com preço unitário positivo. Escolher proposta"
                        + " define fornecedor/preço no mesmo pedido, sem aprová-lo nem criar outro"
                        + " pedido. Aprovação permanece separada em Pedidos. UUID e versão protegem"
                        + " repetição e concorrência.")
@AuditadoNoServico
@RestController
@RequestMapping("/aratech/compras/cotacoes")
@RequiredArgsConstructor
public class CotacaoController {
    private final CotacaoService service;

    @Operation(
            summary = "Consultar cotações vinculadas aos pedidos",
            description =
                    "Listagem paginada; preços são unitários e a quantidade vem do pedido, sem"
                            + " cópia editável.")
    @GetMapping
    public Pagina<Cotacao> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.listar(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Consultar pedidos disponíveis para cotação",
            description =
                    "Somente identificador e item de pedidos ainda não decididos; usa permissão de"
                            + " consulta de cotações.")
    @GetMapping("/pedidos")
    public List<Opcao> pedidos() {
        return service.pedidos();
    }

    @Operation(
            summary = "Registrar propostas de fornecedores",
            description =
                    "Primeira proposta obrigatória; segunda e terceira exigem fornecedor/preço"
                        + " juntos. Limite monetário igual ao preço unitário de Compras. Código do"
                        + " pedido deve existir e estar aberto. Retorna 200.")
    @PostMapping
    public Cotacao criar(@Valid @RequestBody NovaCotacao dto) {
        return service.criar(dto);
    }

    @Operation(
            summary = "Escolher proposta para o pedido",
            description =
                    "Exige FINALIZAR, escolha 1/2/3, versão, UUID e motivo. Atualiza fornecedor e"
                            + " preço do pedido sem conceder aprovação; pedido já decidido retorna"
                            + " 409.")
    @PostMapping("/{id}/escolher")
    public Cotacao escolher(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(id, "escolher", dto);
    }

    @Operation(
            summary = "Cancelar cotação aberta",
            description =
                    "Exige ALTERAR e motivo; preserva histórico e não cancela automaticamente o"
                            + " pedido de origem.")
    @PostMapping("/{id}/cancelar")
    public Cotacao cancelar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(id, "cancelar", dto);
    }
}
