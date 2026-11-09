package com.aratech.moveis.administracao.gestao.controller;

import com.aratech.moveis.administracao.gestao.dto.Auditoria;
import com.aratech.moveis.administracao.gestao.dto.Parametro;
import com.aratech.moveis.administracao.gestao.dto.ValorParametro;
import com.aratech.moveis.administracao.gestao.service.AdministracaoService;
import com.aratech.moveis.pcp.producao.dto.Ordem;
import com.aratech.moveis.pcp.producao.service.ProducaoService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Indicador;
import com.aratech.moveis.shared.dto.Pagina;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Administração — Gestão operacional",
        description =
                "Consultas gerenciais reais, auditoria de ações e parâmetros efetivamente"
                    + " utilizados. Permissões detalhadas separam relatórios, auditoria, parâmetros"
                    + ". Lotes de produção ficam no PCP. Relatórios agregam dados de vários setores e devem ser concedidos"
                    + " apenas a gestores autorizados. Não expõe dados médicos, salários, senhas ou"
                    + " documentos pessoais na auditoria.")
@AuditadoNoServico
@RestController
@RequestMapping("/aratech/administracao")
@RequiredArgsConstructor
public class AdministracaoController {
    private final AdministracaoService service;

    @Operation(
            summary = "Consultar auditoria operacional",
            description =
                    "Eventos paginados, mais recentes primeiro. Ações dos novos fluxos gravam"
                            + " auditoria na mesma transação; requisições legadas bem-sucedidas têm"
                            + " metadados de auditoria sem corpo sensível.")
    @GetMapping("/auditoria")
    public Pagina<Auditoria> auditoria(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.auditoria(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Consultar parâmetros com efeito real",
            description =
                    "pcp.antecedencia_dias e financeiro.antecedencia_dias definem janela de alerta"
                            + " nos relatórios. Não há parâmetros sem implementação.")
    @GetMapping("/parametros")
    public List<Parametro> parametros() {
        return service.parametros();
    }

    @Operation(
            summary = "Alterar janela de alertas",
            description =
                    "Somente chaves cadastradas, valor de 0 a 90 dias, versão atual e UUID. Não"
                            + " permite criar parâmetros arbitrários ou mudar políticas de"
                            + " aprovação/segurança.")
    @PutMapping("/parametros/{chave}")
    public Parametro alterar(@PathVariable String chave, @Valid @RequestBody ValorParametro dto) {
        return service.parametro(chave, dto);
    }

    @Operation(
            summary = "Consultar relatório gerencial consolidado",
            description =
                    "Pendências de compra, compras legadas sem provisão, ordens atrasadas/próximas"
                            + " do prazo, valores vencidos, pagamentos próximos e solicitações de"
                            + " afastamento. A janela usa parâmetros da Administração.")
    @GetMapping("/relatorios")
    public List<Indicador> relatorios() {
        return service.relatorio();
    }

}
