package com.aratech.moveis.comercial.pedidos.controller;

import com.aratech.moveis.comercial.pedidos.dto.NovaVenda;
import com.aratech.moveis.comercial.pedidos.dto.Venda;
import com.aratech.moveis.comercial.pedidos.service.ComercialService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Pagina;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Pedidos legados — Encomendas",
        description =
                "API legada preservada; o módulo principal da aplicação agora é Corte. Não converte encomendas em lotes de corte. RASCUNHO → CONFIRMADO → EM_PRODUCAO → PRONTO → EXPEDIDO. Confirmar cria uma ordem"
                    + " planejada para programação pelo PCP e uma conta a receber; não equivale a"
                    + " pagamento ou emissão fiscal. Cancelar é permitido antes da produção e sem"
                    + " título baixado, cancelando ordem e provisão na mesma transação. UUID"
                    + " estável garante execução única, versão protege concorrência. Saída de"
                    + " fábrica não comprova entrega ao cliente.")
@AuditadoNoServico
@RestController
@RequestMapping("/aratech/comercial/pedidos")
@RequiredArgsConstructor
public class ComercialController {
    private final ComercialService service;

    @Operation(
            summary = "Consultar pedidos de clientes",
            description =
                    "Listagem paginada e busca textual; informações comerciais restritas ao perfil"
                            + " autorizado.")
    @GetMapping
    public Pagina<Venda> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.vendas(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Cadastrar encomenda em rascunho",
            description =
                    "Cliente, produto, quantidade positiva, preço unitário até 999999.99, entrega"
                            + " não passada e vencimento negociado. Total limitado ao contrato"
                            + " financeiro. Retorna 200.")
    @PostMapping
    public Venda criar(@Valid @RequestBody NovaVenda dto) {
        return service.venda(null, dto);
    }

    @Operation(
            summary = "Editar encomenda em rascunho",
            description =
                    "Não altera encomenda confirmada, em fabricação ou expedida. Exige versão"
                            + " atual.")
    @PutMapping("/{id}")
    public Venda editar(@PathVariable long id, @Valid @RequestBody NovaVenda dto) {
        return service.venda(id, dto);
    }

    @Operation(
            summary = "Confirmar encomenda e integrar PCP/Financeiro",
            description =
                    "Exige FINALIZAR. Cria ordem PLANEJADA sem linha e com horas provisórias (PCP"
                            + " deve programar antes de iniciar), e um título a receber único. Não"
                            + " exige conceder acesso ao Financeiro ou PCP ao vendedor.")
    @PostMapping("/{id}/confirmar")
    public Venda confirmar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.vendaAcao(id, "confirmar", dto);
    }

    @Operation(
            summary = "Cancelar encomenda antes da fabricação",
            description =
                    "Exige ALTERAR e motivo. Somente RASCUNHO/CONFIRMADO, ordem ainda não iniciada"
                        + " e provisão ainda não baixada. Preserva histórico e cancela ordem/título"
                        + " integrados. Encomenda com lote setorial vinculado não pode ser cancelada por este fluxo (409).")
    @PostMapping("/{id}/cancelar")
    public Venda cancelar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.vendaAcao(id, "cancelar", dto);
    }
}
