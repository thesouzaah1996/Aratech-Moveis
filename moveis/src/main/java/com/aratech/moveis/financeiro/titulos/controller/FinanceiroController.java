package com.aratech.moveis.financeiro.titulos.controller;

import com.aratech.moveis.financeiro.titulos.dto.Caixa;
import com.aratech.moveis.financeiro.titulos.dto.Documento;
import com.aratech.moveis.financeiro.titulos.dto.NovoTitulo;
import com.aratech.moveis.financeiro.titulos.dto.Titulo;
import com.aratech.moveis.financeiro.titulos.service.FinanceiroService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Pagina;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Financeiro — Títulos e caixa",
        description =
                "Controle gerencial, sem emissão fiscal ou transação bancária. Títulos integrados"
                    + " de venda/compra são únicos por origem. Baixa integral registra data real e"
                    + " motivo; não admite duplicidade nem data futura. Pendências vencidas são"
                    + " calculadas na consulta. Cada escrita exige UUID estável e versão; conflitos"
                    + " retornam 409. Valores positivos com duas casas, até 9999999999999.99."
                    + " Documentos de fornecedor não podem se repetir entre títulos ativos.")
@AuditadoNoServico
@RestController
@RequestMapping("/aratech/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {
    private final FinanceiroService service;

    @Operation(
            summary = "Consultar contas ou documentos",
            description =
                    "tipo: pagar, receber ou notas. Notas são os mesmos títulos a pagar com número"
                            + " de documento, evitando dupla contagem. Listagem paginada e busca"
                            + " textual.")
    @GetMapping("/titulos/{tipo}")
    public Pagina<Titulo> listar(
            @PathVariable String tipo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.listar(tipo, pagina, tamanho, busca);
    }

    @Operation(
            summary = "Registrar título manual",
            description =
                    "Cria pendente; notas exigem documento e emissão. Vencimento não pode anteceder"
                            + " emissão. Não gera pagamento bancário. Retorna 200, inclusive na"
                            + " repetição idempotente.")
    @PostMapping("/titulos/{tipo}")
    public Titulo criar(@PathVariable String tipo, @Valid @RequestBody NovoTitulo dto) {
        return service.salvar(tipo, null, dto);
    }

    @Operation(
            summary = "Editar título manual pendente",
            description =
                    "Títulos de compra/venda são controlados pela origem. Apenas manual pendente e"
                            + " versão atual; dados imutáveis de origem não são aceitos no corpo.")
    @PutMapping("/titulos/{tipo}/{id}")
    public Titulo editar(
            @PathVariable String tipo, @PathVariable long id, @Valid @RequestBody NovoTitulo dto) {
        return service.salvar(tipo, id, dto);
    }

    @Operation(
            summary = "Baixar título integralmente",
            description =
                    "Exige FINALIZAR. data deve representar pagamento/recebimento já realizado, não"
                            + " futura; motivo obrigatório. Baixa única gera movimento de caixa"
                            + " derivado, sem duplicar lançamento.")
    @PostMapping("/titulos/{tipo}/{id}/baixar")
    public Titulo baixar(
            @PathVariable String tipo, @PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(tipo, id, "baixar", dto);
    }

    @Operation(
            summary = "Cancelar título manual pendente",
            description =
                    "Exige ALTERAR, motivo e versão. Não remove histórico e não cancela título já"
                            + " baixado ou integrado.")
    @PostMapping("/titulos/{tipo}/{id}/cancelar")
    public Titulo cancelar(
            @PathVariable String tipo, @PathVariable long id, @Valid @RequestBody Comando dto) {
        return service.acao(tipo, id, "cancelar", dto);
    }

    @Operation(
            summary = "Vincular documento ao título existente",
            description =
                    "Exige ALTERAR. Permite anexar número e emissão à obrigação pendente, inclusive"
                            + " integrada de compra, sem criar outro valor a pagar. Confere versão,"
                            + " datas e duplicidade de documento; não emite NF.")
    @PostMapping("/titulos/{tipo}/{id}/documentar")
    public Titulo documentar(
            @PathVariable String tipo, @PathVariable long id, @Valid @RequestBody Documento dto) {
        return service.documentar(tipo, id, dto);
    }

    @Operation(
            summary = "Consultar caixa realizado acumulado até o mês",
            description =
                    "Somente títulos baixados até o fim de mes (YYYY-MM). Saldo inclui movimentos"
                        + " anteriores, com entrada positiva e saída negativa; valores representam"
                        + " caixa registrado, não saldo bancário conciliado.")
    @GetMapping("/caixa")
    public Pagina<Caixa> caixa(
            @RequestParam String mes,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(defaultValue = "") String busca) {
        return service.caixa(mes, pagina, tamanho, busca);
    }
}
