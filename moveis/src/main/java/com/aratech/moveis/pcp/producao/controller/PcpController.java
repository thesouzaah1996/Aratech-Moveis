package com.aratech.moveis.pcp.producao.controller;

import com.aratech.moveis.pcp.producao.dto.Carga;
import com.aratech.moveis.pcp.producao.dto.Linha;
import com.aratech.moveis.pcp.producao.dto.NovaLinha;
import com.aratech.moveis.pcp.producao.dto.NovaOrdem;
import com.aratech.moveis.pcp.producao.dto.Ordem;
import com.aratech.moveis.pcp.producao.service.ProducaoService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Indicador;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "PCP — Produção e programação",
        description =
                "Ordens reais com fluxo PLANEJADA → EM_PRODUCAO → FINALIZADA. Somente planejadas"
                    + " podem ser reprogramadas; cancelamento direto apenas planejadas sem"
                    + " encomenda. Horas previstas são distribuídas pelos dias corridos, sem"
                    + " presumir turnos. Linha com capacidade diária de até 24h; programação não"
                    + " pode exceder as horas disponíveis. Conclusão gera lote lógico OP-{id} e"
                    + " disponibiliza no Carregamento, sem criar estoque de insumos ou lotes de"
                    + " Assistência. UUID e versão obrigatórios, conflito retorna 409.")
@AuditadoNoServico
@RestController
@RequestMapping("/aratech/pcp")
@RequiredArgsConstructor
public class PcpController {
    private final ProducaoService service;

    @Operation(summary = "Consultar lotes de produção concluída no PCP", description = "Exige PCP_LOTES.CONSULTAR. Consulta movida da Administração: cada ordem concluída corresponde ao lote OP-{id}, com datas de conclusão e expedição. Mesmos dados existentes, sem cópia ou conversão para lotes de corte. Saída continua no Carregamento.")
    @GetMapping("/lotes")
    public Pagina<Ordem> lotes(@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="8") int tamanho,@RequestParam(defaultValue="") String busca) {
        return service.ordens("lotes",pagina,tamanho,busca);
    }

    @Operation(
            summary = "Consultar ordens de produção",
            description =
                    "Lista paginada; busca por produto, responsável, linha e situação. Sem"
                            + " registros fictícios.")
    @GetMapping("/ordens")
    public Pagina<Ordem> ordens(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.ordens("todas", pagina, tamanho, busca);
    }

    @Operation(
            summary = "Criar ordem para estoque",
            description =
                    "Cria PLANEJADA, com produto, quantidade inteira positiva, período de até 366"
                            + " dias, responsável e estimativa de horas. Linha é opcional no"
                            + " planejamento inicial, obrigatória para iniciar.")
    @PostMapping("/ordens")
    public Ordem criar(@Valid @RequestBody NovaOrdem dto) {
        return service.salvar(null, dto);
    }

    @Operation(
            summary = "Reprogramar ordem planejada",
            description =
                    "Valida capacidade da linha em cada dia do intervalo, sob bloqueio"
                        + " transacional. Em encomenda, produto e quantidade não podem divergir da"
                        + " venda. Não edita ordem em execução. Lotes vinculados impedem trocar o produto ou reduzir abaixo da quantidade já planejada.")
    @PutMapping("/ordens/{id}")
    public Ordem editar(@PathVariable long id, @Valid @RequestBody NovaOrdem dto) {
        return service.salvar(id, dto);
    }

    @Operation(
            summary = "Iniciar produção",
            description =
                    "Exige ALTERAR. Somente PLANEJADA com linha definida e início programado não"
                            + " futuro. Atualiza encomenda vinculada para EM_PRODUCAO.")
    @PostMapping("/ordens/{id}/iniciar")
    public Ordem iniciar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(id, "iniciar", dto);
    }

    @Operation(
            summary = "Concluir produção integral da ordem",
            description =
                    "Exige FINALIZAR. Somente EM_PRODUCAO; registra instante e disponibiliza lote"
                            + " na expedição, atualizando venda para PRONTO. Não baixa materiais"
                            + " automaticamente sem ficha técnica. Todos os lotes setoriais vinculados precisam estar concluídos; setores pendentes impedem concluir (409).")
    @PostMapping("/ordens/{id}/concluir")
    public Ordem concluir(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(id, "concluir", dto);
    }

    @Operation(
            summary = "Cancelar ordem planejada para estoque",
            description =
                    "Exige ALTERAR e motivo. Encomendas são canceladas no Comercial, sincronizando"
                            + " a provisão financeira. Lotes setoriais vinculados impedem cancelar a ordem (409).")
    @PostMapping("/ordens/{id}/cancelar")
    public Ordem cancelar(@PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(id, "cancelar", dto);
    }

    @Operation(
            summary = "Consultar programação de ordens abertas",
            description =
                    "Agenda derivada das mesmas ordens; não mantém cópia divergente. Reprogramação"
                            + " ocorre em Ordens.")
    @GetMapping("/programacao")
    public Pagina<Ordem> programacao(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.ordens("programacao", pagina, tamanho, busca);
    }

    @Operation(
            summary = "Consultar linhas de produção",
            description = "Capacidade cadastrada em horas por dia, sem dados demonstrativos.")
    @GetMapping("/linhas")
    public Pagina<Linha> linhas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.linhas(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Consultar opções de linha para planejamento",
            description =
                    "Lista apenas id e nome de linhas ativas. Usa permissão de consultar ordens,"
                            + " sem expor dados de outro setor.")
    @GetMapping("/ordens/linhas")
    public List<Opcao> opcoes() {
        return service.opcoesLinhas();
    }

    @Operation(
            summary = "Cadastrar linha e capacidade diária",
            description =
                    "Nome único, horas positivas até 24. Retorna 200; repetição idempotente não"
                            + " duplica cadastro.")
    @PostMapping("/linhas")
    public Linha linha(@Valid @RequestBody NovaLinha dto) {
        return service.linha(null, dto);
    }

    @Operation(
            summary = "Editar capacidade de linha sem ordens abertas",
            description =
                    "Rejeita nome duplicado, versão antiga ou linha com ordens planejadas/em"
                            + " execução; reprograme as ordens antes de reduzir capacidade.")
    @PutMapping("/linhas/{id}")
    public Linha linhaEditar(@PathVariable long id, @Valid @RequestBody NovaLinha dto) {
        return service.linha(id, dto);
    }

    @Operation(
            summary = "Calcular carga produtiva do dia",
            description =
                    "Horas estimadas por dia corrido das ordens abertas no dia atual; saldo é"
                        + " capacidade cadastrada menos carga. Não representa OEE ou produtividade"
                        + " medida.")
    @GetMapping("/capacidade")
    public List<Carga> capacidade() {
        return service.capacidade();
    }

    @Operation(
            summary = "Calcular indicadores reais de produção",
            description =
                    "Ordens planejadas, em execução, atrasadas, unidades concluídas no mês e ordens"
                            + " prontas para expedição. Sem métricas fixas ou OEE sem medição.")
    @GetMapping("/indicadores")
    public List<Indicador> indicadores() {
        return service.indicadores();
    }
}
