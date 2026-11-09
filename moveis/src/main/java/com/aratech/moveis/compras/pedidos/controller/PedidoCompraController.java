package com.aratech.moveis.compras.pedidos.controller;

import com.aratech.moveis.compras.pedidos.dto.Cadastro;
import com.aratech.moveis.compras.pedidos.dto.Decisao;
import com.aratech.moveis.compras.pedidos.dto.Pedido;
import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;
import com.aratech.moveis.security.dto.UsuarioSessao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/aratech/compras/pedidos")
@Tag(
        name = "Compras — Pedidos",
        description =
                "Centraliza pedidos manuais, reposições por estoque mínimo e solicitações diretas"
                    + " de peças. Origem e item são preservados. Decisão exige COMPRAS.FINALIZAR,"
                    + " versão atual e status compatível; reprovação exige motivo de 5 a 1000"
                    + " caracteres. A origem consulta os mesmos registros. Aprovar/receber não"
                    + " credita estoque: o recebimento físico deve ser lançado como entrada no"
                    + " módulo de estoque.")
public class PedidoCompraController {
    private final PedidoCompraService service;

    @Operation(
            summary = "Listar pedidos de compra",
            description =
                    "Retorna todos os pedidos, mais recentes primeiro. A interface aplica pesquisa,"
                        + " status e paginação visual. Valores e fornecedor podem estar vazios em"
                        + " solicitações automáticas ainda não cotadas.")
    @GetMapping
    public List<Pedido> listar() {
        return service.listar(null);
    }

    @Operation(
            summary = "Criar pedido manual",
            description =
                    "Cria Rascunho de origem COMPRAS. Exige item, fornecedor, quantidade positiva"
                            + " com até 3 casas, valor positivo com até 2 casas e entrega igual ou"
                            + " posterior à data do pedido.")
    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Pedido criar(@RequestBody Cadastro dto, @AuthenticationPrincipal UsuarioSessao autor) {
        return service.salvar(null, dto, autor);
    }

    @Operation(
            summary = "Editar pedido antes da decisão",
            description =
                    "Aceita Rascunho ou Aguardando Aprovação, com versão atual. Origem, tipo e"
                        + " referência não são alteráveis; item de origem não pode ser substituído."
                        + " Atualiza quantidade, fornecedor, valor e datas.")
    @PutMapping("/{id}")
    public Pedido editar(
            @PathVariable Long id,
            @RequestBody Cadastro dto,
            @AuthenticationPrincipal UsuarioSessao autor) {
        return service.salvar(id, dto, autor);
    }

    @Operation(
            summary = "Enviar rascunho para aprovação",
            description =
                    "Muda Rascunho para Aguardando Aprovação. Exige versão atual; pedidos"
                        + " automáticos e solicitações de peças já chegam aguardando aprovação.")
    @PatchMapping("/{id}/enviar")
    public Pedido enviar(@PathVariable Long id, @RequestBody Decisao dto) {
        return service.enviar(id, dto.versao());
    }

    @Operation(
            summary = "Remover rascunho manual",
            description =
                    "Cancela somente rascunhos manuais, preservando o registro e o histórico de"
                            + " origem. Requer versão atual na query.")
    @DeleteMapping("/{id}")
    public Pedido cancelar(@PathVariable Long id, @RequestParam long versao) {
        return service.cancelar(id, versao);
    }

    @Operation(
            summary = "Aprovar pedido",
            description =
                    "Somente Aguardando Aprovação, com versão atual e COMPRAS_PEDIDOS.FINALIZAR (ou"
                        + " permissão ampla). Exige fornecedor, preço, entrega e vencimento"
                        + " negociado preenchidos. Registra responsável e instante; não movimenta"
                        + " estoque.")
    @PostMapping("/{id}/aprovar")
    public Pedido aprovar(
            @PathVariable Long id,
            @RequestBody Decisao dto,
            @AuthenticationPrincipal UsuarioSessao autor) {
        return service.decidir(id, "aprovar", dto, autor);
    }

    @Operation(
            summary = "Reprovar pedido com justificativa",
            description =
                    "Somente Aguardando Aprovação. Motivo obrigatório, 5 a 1000 caracteres."
                            + " Registra autor e instante e disponibiliza o motivo nas consultas do"
                            + " Almoxarifado e da Manutenção. Não recria automaticamente o pedido"
                            + " rejeitado enquanto o item permanecer abaixo do mínimo.")
    @PostMapping("/{id}/reprovar")
    public Pedido reprovar(
            @PathVariable Long id,
            @RequestBody Decisao dto,
            @AuthenticationPrincipal UsuarioSessao autor) {
        return service.decidir(id, "reprovar", dto, autor);
    }

    @Operation(
            summary = "Registrar recebimento do pedido aprovado",
            description =
                    "Muda Aprovado para Recebido e gera uma única conta a pagar, usando quantidade"
                        + " × preço unitário e o vencimento negociado. Exige dados financeiros"
                        + " completos; não confunde vencimento com entrega. Não gera entrada"
                        + " automática de estoque; registre a quantidade física recebida no módulo"
                        + " de origem.")
    @PostMapping("/{id}/receber")
    public Pedido receber(
            @PathVariable Long id,
            @RequestBody Decisao dto,
            @AuthenticationPrincipal UsuarioSessao autor) {
        return service.decidir(id, "receber", dto, autor);
    }
}
