package com.aratech.moveis.rh.solicitacoes.controller;

import com.aratech.moveis.rh.solicitacoes.dto.NovaSolicitacao;
import com.aratech.moveis.rh.solicitacoes.dto.Solicitacao;
import com.aratech.moveis.rh.solicitacoes.service.SolicitacaoRhService;
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
        name = "RH — Solicitações",
        description =
                "Registros administrativos vinculados a funcionário ativo; não calculam direitos"
                    + " trabalhistas, folha ou férias legais. Afastamento: PENDENTE →"
                    + " APROVADO/RECUSADO/CANCELADO; EM_CURSO e CONCLUIDO são derivados das datas"
                    + " aprovadas. Não permite períodos sobrepostos pendentes/aprovados."
                    + " Solicitação: PENDENTE → EM_ANALISE → APROVADO/RECUSADO, com resposta"
                    + " obrigatória. Informações restritas ao RH autorizado; não concede acesso"
                    + " geral a empregados. UUID estável e versão garantem concorrência e repetição"
                    + " sem duplicação.")
@AuditadoNoServico
@RestController
@RequestMapping("/aratech/rh")
@RequiredArgsConstructor
public class SolicitacaoRhController {
    private final SolicitacaoRhService service;

    @Operation(
            summary = "Consultar solicitações do RH",
            description =
                    "Dados administrativos restritos ao RH autorizado, com paginação e busca.")
    @GetMapping("/solicitacoes")
    public Pagina<Solicitacao> solicitacoes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.solicitacoes(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Registrar solicitação administrativa",
            description =
                    "Vincula funcionário ativo, tipo e descrição de 3 a 1000 caracteres. Cria"
                        + " PENDENTE; não gera documentos fiscais ou holerites automaticamente.")
    @PostMapping("/solicitacoes")
    public Solicitacao solicitar(@Valid @RequestBody NovaSolicitacao dto) {
        return service.solicitar(dto);
    }

    @Operation(
            summary = "Iniciar análise de solicitação",
            description = "Exige ALTERAR; somente pendente, com observação obrigatória.")
    @PostMapping("/solicitacoes/{id}/iniciar")
    public Solicitacao iniciar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.solicitacaoAcao(id, "iniciar", dto);
    }

    @Operation(
            summary = "Concluir solicitação aprovada",
            description =
                    "Exige FINALIZAR e resposta de 3 a 1000 caracteres. Somente"
                            + " PENDENTE/EM_ANALISE.")
    @PostMapping("/solicitacoes/{id}/aprovar")
    public Solicitacao aprovarS(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.solicitacaoAcao(id, "aprovar", dto);
    }

    @Operation(
            summary = "Concluir solicitação recusada",
            description = "Exige FINALIZAR e justificativa. Não apaga o histórico da decisão.")
    @PostMapping("/solicitacoes/{id}/reprovar")
    public Solicitacao reprovarS(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.solicitacaoAcao(id, "reprovar", dto);
    }

    @Operation(
            summary = "Opções de funcionários para solicitação",
            description =
                    "Somente id e nome de funcionários ativos, com permissão de consultar"
                            + " solicitações.")
    @GetMapping("/solicitacoes/funcionarios")
    public List<Opcao> funcionariosS() {
        return service.funcionarios();
    }
}
