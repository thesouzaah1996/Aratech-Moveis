package com.aratech.moveis.compras.pedidos.controller;

import com.aratech.moveis.compras.pedidos.dto.Minimo;
import com.aratech.moveis.compras.pedidos.dto.Pedido;
import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "Estoque — Reposição e acompanhamento",
        description =
                "Mínimo é opcional: null desativa monitoramento; zero monitora estoque zerado."
                    + " Saldo menor ou igual ao mínimo gera pedido imediato em Compras. Quantidade"
                    + " sugerida = mínimo − saldo + 1. Um pedido por episódio de estoque baixo;"
                    + " rejeitados não se repetem até o saldo superar o mínimo e cair novamente."
                    + " Pedido pendente é atualizado se o saldo cair mais e cancelado se reposto"
                    + " acima do mínimo. Pedido aprovado permanece até recebimento. Operações usam"
                    + " a permissão do módulo de origem.")
public class PedidosOrigemController {
    private final PedidoCompraService service;

    @Operation(
            summary = "Consultar pedidos do Almoxarifado",
            description =
                    "Mostra somente pedidos de origem ALMOXARIFADO, incluindo decisão, motivo de"
                            + " reprovação, responsável e data.")
    @GetMapping("/aratech/almoxarifado/pedidos")
    public List<Pedido> almoxarifado() {
        return service.listar("ALMOXARIFADO");
    }

    @Operation(
            summary = "Consultar pedidos da Manutenção",
            description =
                    "Mostra reposições de estoque e solicitações de peças de origem MANUTENCAO, com"
                            + " a mesma decisão e motivo gravados por Compras.")
    @GetMapping("/aratech/manutencao/pedidos")
    public List<Pedido> manutencao() {
        return service.listar("MANUTENCAO");
    }

    @Operation(
            summary = "Definir quantidade mínima de produto",
            description =
                    "Recebe quantidadeMinima: inteiro de 0 a 999999999, ou null para desativar. A"
                        + " célula da interface é editável com duplo clique. Apenas item ativo."
                        + " Verifica o saldo na mesma transação e cria a reposição imediatamente"
                        + " quando saldo <= mínimo.")
    @PatchMapping("/aratech/almoxarifado/produtos/{id}/minimo")
    public Minimo produto(@PathVariable Long id, @RequestBody Minimo dto) {
        return service.minimo("ALMOXARIFADO", id, dto.quantidadeMinima());
    }

    @Operation(
            summary = "Definir quantidade mínima de peça de manutenção",
            description =
                    "Recebe quantidadeMinima: valor não negativo com até 3 casas e máximo"
                            + " 999999999998.999, ou null para desativar. Verifica reposição"
                            + " imediatamente; não altera o saldo físico.")
    @PatchMapping("/aratech/manutencao/estoque/{id}/minimo")
    public Minimo peca(@PathVariable Long id, @RequestBody Minimo dto) {
        return service.minimo("MANUTENCAO", id, dto.quantidadeMinima());
    }
}
