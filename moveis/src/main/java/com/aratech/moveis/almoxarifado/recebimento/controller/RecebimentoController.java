package com.aratech.moveis.almoxarifado.recebimento.controller;

import com.aratech.moveis.almoxarifado.recebimento.service.RecebimentoService;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.dto.FilaResumoDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Almoxarifado — Recebimentos",
        description =
                "Consulta somente chegadas do setor ALMOXARIFADO. Fluxo: AGUARDANDO → AUTORIZADO"
                    + " pelo setor → EM_RECEBIMENTO pela portaria → FINALIZADO pelo setor. Edição"
                    + " somente em AGUARDANDO, sem mudar o setor. A fila mantém os mais antigos"
                    + " primeiro; histórico mostra finalizados.")
@RestController
@RequestMapping("/aratech/almoxarifado/recebimentos")
@RequiredArgsConstructor
public class RecebimentoController {
    private final RecebimentoService service;

    @Operation(
            summary = "Listar recebimentos",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Page<ChegadaResumoDTO> fila(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho) {
        return service.fila(pagina, tamanho);
    }

    @Operation(
            summary = "Consultar histórico de recebimento",
            description =
                    "Retorna o histórico persistido, preservando operações anteriores. Veja os"
                            + " filtros e a paginação disponíveis.")
    @GetMapping("/historico")
    public Page<ChegadaResumoDTO> historico(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho) {
        return service.historico(pagina, tamanho);
    }

    @Operation(
            summary = "Consultar resumo da fila",
            description =
                    "Retorna os contadores e a última referência da fila no setor correspondente.")
    @GetMapping("/resumo")
    public FilaResumoDTO resumo() {
        return service.resumo();
    }

    @Operation(
            summary = "Atualizar recebimento",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping("/{id}")
    public ChegadaResumoDTO editar(@PathVariable Long id, @RequestBody ChegadaDTO dto) {
        return service.editar(id, dto);
    }

    @Operation(
            summary = "Autorizar chegada no setor",
            description =
                    "Muda AGUARDANDO para AUTORIZADO. Confirmação da entrada continua sendo feita"
                            + " na portaria; não pula etapas.")
    @PostMapping("/{id}/autorizar")
    public ChegadaResumoDTO autorizar(@PathVariable Long id) {
        return service.autorizar(id);
    }

    @Operation(
            summary = "Finalizar atendimento da chegada",
            description =
                    "Exige EM_RECEBIMENTO e setor correspondente. Registra finalização e"
                        + " disponibiliza no histórico. Repetir a transição já aplicada devolve o"
                        + " registro.")
    @PostMapping("/{id}/finalizar")
    public ChegadaResumoDTO finalizar(@PathVariable Long id) {
        return service.finalizar(id);
    }
}
