package com.aratech.moveis.carregamento.controller;

import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.dto.FilaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.service.ControleAcessoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Carregamento — Chegadas",
        description =
                "Consulta somente chegadas do setor CARREGAMENTO, com tipoOperacao CARREGAR ou"
                    + " DESCARREGAR. Autorizar não confirma entrada: a portaria confirma a entrada"
                    + " antes da finalização. Edição somente em AGUARDANDO e sem alterar setor. A"
                    + " fila ordena dataChegada e ID crescentes.")
@RestController
@RequestMapping("/aratech/carregamento/chegadas")
@RequiredArgsConstructor
public class CarregamentoController {
    private final ControleAcessoService service;

    @Operation(
            summary = "Listar chegadas do carregamento",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Page<ChegadaResumoDTO> fila(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho) {
        return service.filaSetor("CARREGAMENTO", pagina, tamanho);
    }

    @Operation(
            summary = "Consultar histórico de chegada de carregamento",
            description =
                    "Retorna o histórico persistido, preservando operações anteriores. Veja os"
                            + " filtros e a paginação disponíveis.")
    @GetMapping("/historico")
    public Page<ChegadaResumoDTO> historico(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho) {
        return service.historicoSetor("CARREGAMENTO", pagina, tamanho);
    }

    @Operation(
            summary = "Consultar resumo da fila",
            description =
                    "Retorna os contadores e a última referência da fila no setor correspondente.")
    @GetMapping("/resumo")
    public FilaResumoDTO resumo() {
        return service.resumoSetor("CARREGAMENTO");
    }

    @Operation(
            summary = "Atualizar chegada de carregamento",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping("/{id}")
    public ChegadaResumoDTO editar(@PathVariable Long id, @RequestBody ChegadaDTO dto) {
        return service.editarSetor("CARREGAMENTO", id, dto);
    }

    @Operation(
            summary = "Autorizar chegada no setor",
            description =
                    "Muda AGUARDANDO para AUTORIZADO. Confirmação da entrada continua sendo feita"
                            + " na portaria; não pula etapas.")
    @PostMapping("/{id}/autorizar")
    public ChegadaResumoDTO autorizar(@PathVariable Long id) {
        return service.autorizarSetor("CARREGAMENTO", id);
    }

    @Operation(
            summary = "Finalizar atendimento da chegada",
            description =
                    "Exige EM_RECEBIMENTO e setor correspondente. Registra finalização e"
                        + " disponibiliza no histórico. Repetir a transição já aplicada devolve o"
                        + " registro.")
    @PostMapping("/{id}/finalizar")
    public ChegadaResumoDTO finalizar(@PathVariable Long id) {
        return service.finalizarSetor("CARREGAMENTO", id);
    }
}
