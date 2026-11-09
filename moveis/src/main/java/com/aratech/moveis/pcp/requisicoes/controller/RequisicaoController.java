package com.aratech.moveis.pcp.requisicoes.controller;

import com.aratech.moveis.pcp.requisicoes.dto.NovaRequisicao;
import com.aratech.moveis.pcp.requisicoes.dto.Requisicao;
import com.aratech.moveis.pcp.requisicoes.service.RequisicaoService;
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
        name = "Produção e Almoxarifado — Requisições",
        description =
                "PCP solicita material de ordem aberta; Almoxarifado atende integralmente ou"
                    + " recusa. Atendimento usa a saída real do estoque na mesma transação,"
                    + " preservando histórico e monitoramento de mínimo/Compras. Devolução integral"
                    + " antes de iniciar a ordem usa entrada real. Sem consumo presumido por"
                    + " conclusão da produção. UUID e versão impedem dupla baixa; não há exclusão"
                    + " física.")
@AuditadoNoServico
@RestController
@RequiredArgsConstructor
public class RequisicaoController {
    private final RequisicaoService service;

    @Operation(
            summary = "Consultar requisições no PCP",
            description =
                    "Paginadas e pesquisáveis; somente material, ordem, quantidade e decisão, sem"
                            + " preços de compra.")
    @GetMapping("/aratech/pcp/requisicoes")
    public Pagina<Requisicao> pcp(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.listar(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Solicitar material para ordem aberta",
            description =
                    "Exige cadastrar requisições do PCP. Quantidade inteira positiva; ordem"
                            + " PLANEJADA/EM_PRODUCAO e material ativo. Não altera estoque até"
                            + " atendimento pelo Almoxarifado.")
    @PostMapping("/aratech/pcp/requisicoes")
    public Requisicao criar(@Valid @RequestBody NovaRequisicao dto) {
        return service.criar(dto);
    }

    @Operation(
            summary = "Ordens disponíveis para requisição",
            description =
                    "Somente id e produto de ordens abertas; não exige liberar outras telas do"
                            + " PCP.")
    @GetMapping("/aratech/pcp/requisicoes/ordens")
    public List<Opcao> ordens() {
        return service.ordens();
    }

    @Operation(
            summary = "Materiais disponíveis para requisição",
            description =
                    "Somente id, código, nome e saldo de produtos ativos do Almoxarifado. Não"
                            + " retorna fornecedor, preços ou outros cadastros.")
    @GetMapping("/aratech/pcp/requisicoes/materiais")
    public List<Opcao> materiais() {
        return service.materiais();
    }

    @Operation(
            summary = "Consultar requisições no Almoxarifado",
            description =
                    "Fila persistida compartilhada com o PCP; decisões aparecem para o solicitante"
                            + " sem duplicar solicitações.")
    @GetMapping("/aratech/almoxarifado/requisicoes")
    public Pagina<Requisicao> almox(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.listar(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Atender e baixar material do estoque",
            description =
                    "Exige FINALIZAR no Almoxarifado, motivo, versão e UUID. Apenas pendente e"
                        + " ordem aberta. Saldo insuficiente rejeita tudo; sucesso registra saída e"
                        + " reposição por mínimo quando aplicável.")
    @PostMapping("/aratech/almoxarifado/requisicoes/{id}/atender")
    public Requisicao atender(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(id, "atender", dto);
    }

    @Operation(
            summary = "Recusar requisição com justificativa",
            description = "Exige FINALIZAR e motivo. Apenas pendente; não altera saldo.")
    @PostMapping("/aratech/almoxarifado/requisicoes/{id}/reprovar")
    public Requisicao recusar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(id, "reprovar", dto);
    }

    @Operation(
            summary = "Registrar devolução integral antes da produção",
            description =
                    "Exige FINALIZAR. Apenas ATENDIDA com ordem ainda PLANEJADA; registra entrada"
                            + " de material na mesma transação. Não permite devolução presumida de"
                            + " itens já consumidos na produção.")
    @PostMapping("/aratech/almoxarifado/requisicoes/{id}/devolver")
    public Requisicao devolver(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(id, "devolver", dto);
    }
}
