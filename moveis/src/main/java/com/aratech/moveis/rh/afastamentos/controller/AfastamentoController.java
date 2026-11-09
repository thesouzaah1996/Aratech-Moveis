package com.aratech.moveis.rh.afastamentos.controller;

import com.aratech.moveis.rh.afastamentos.dto.Afastamento;
import com.aratech.moveis.rh.afastamentos.dto.NovoAfastamento;
import com.aratech.moveis.rh.afastamentos.service.AfastamentoService;
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
        name = "RH — Afastamentos",
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
public class AfastamentoController {
    private final AfastamentoService service;

    @Operation(
            summary = "Consultar afastamentos administrativos",
            description =
                    "Paginado; datas derivam situação de períodos aprovados. Busca por funcionário,"
                            + " tipo e situação.")
    @GetMapping("/afastamentos")
    public Pagina<Afastamento> afastamentos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.afastamentos(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Solicitar férias ou afastamento",
            description =
                    "Funcionário ativo, período válido de até 730 dias, tipo"
                        + " FERIAS/LICENCA/AFASTAMENTO e motivo. Não admite sobreposição ativa. Não"
                        + " altera automaticamente ponto ou folha.")
    @PostMapping("/afastamentos")
    public Afastamento criar(@Valid @RequestBody NovoAfastamento dto) {
        return service.criarAfastamento(dto);
    }

    @Operation(
            summary = "Aprovar afastamento pendente",
            description =
                    "Exige FINALIZAR, versão e motivo. Confere funcionário ativo e sobreposição sob"
                            + " bloqueio.")
    @PostMapping("/afastamentos/{id}/aprovar")
    public Afastamento aprovar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.afastamentoAcao(id, "aprovar", dto);
    }

    @Operation(
            summary = "Recusar afastamento pendente",
            description = "Exige FINALIZAR e justificativa. Preserva solicitação e autoria.")
    @PostMapping("/afastamentos/{id}/reprovar")
    public Afastamento reprovar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.afastamentoAcao(id, "reprovar", dto);
    }

    @Operation(
            summary = "Cancelar pedido pendente de afastamento",
            description =
                    "Exige ALTERAR e motivo; não apaga registros aprovados nem recalcula ponto.")
    @PostMapping("/afastamentos/{id}/cancelar")
    public Afastamento cancelar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.afastamentoAcao(id, "cancelar", dto);
    }

    @Operation(
            summary = "Opções de funcionários para afastamento",
            description =
                    "Somente identificador e nome de funcionários ativos, com permissão específica"
                            + " de consultar afastamentos.")
    @GetMapping("/afastamentos/funcionarios")
    public List<Opcao> funcionariosA() {
        return service.funcionarios();
    }
}
