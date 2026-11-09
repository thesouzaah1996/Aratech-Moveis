package com.aratech.moveis.portaria.controleacesso.controller;

import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.dto.FilaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.service.ControleAcessoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(
        name = "Portaria — Controle de acesso",
        description =
                "Nota fiscal aceita número de 1 a 9 dígitos não nulo ou chave de 44 dígitos com"
                    + " verificador válido. Placa brasileira tradicional ou Mercosul. Setor:"
                    + " ALMOXARIFADO ou CARREGAMENTO; no carregamento, informe CARREGAR ou"
                    + " DESCARREGAR. Nota/empresa duplicada e placa com chegada ativa são"
                    + " rejeitadas. Registro usa chaveOperacao idempotente. A entrada depende de"
                    + " autorização do setor. Filtro de data usa America/Sao_Paulo. Fluxo:"
                    + " AGUARDANDO → AUTORIZADO → EM_RECEBIMENTO → FINALIZADO.")
@RestController
@RequestMapping("/aratech/portaria/chegadas")
@RequiredArgsConstructor
public class ControleAcessoController {
    private final ControleAcessoService service;

    @Operation(
            summary = "Listar chegadas",
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
            summary = "Consultar histórico de chegada",
            description =
                    "Retorna o histórico persistido, preservando operações anteriores. Veja os"
                            + " filtros e a paginação disponíveis.")
    @GetMapping("/historico")
    public Page<ChegadaResumoDTO> historico(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(required = false) String notaFiscal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate data) {
        return service.historico(pagina, tamanho, notaFiscal, data);
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
            summary = "Registrar chegada na portaria",
            description =
                    "Cria em AGUARDANDO. Reenvie a mesma chaveOperacao somente com os mesmos dados"
                            + " para evitar duplicação.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChegadaResumoDTO registrar(@RequestBody ChegadaDTO dto) {
        return service.registrar(dto);
    }

    @Operation(
            summary = "Atualizar chegada",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping("/{id}")
    public ChegadaResumoDTO editar(@PathVariable Long id, @RequestBody ChegadaDTO dto) {
        return service.editar(id, dto);
    }

    @Operation(
            summary = "Confirmar entrada autorizada",
            description =
                    "Muda AUTORIZADO para EM_RECEBIMENTO. Chegada ainda AGUARDANDO retorna 409.")
    @PostMapping("/{id}/entrada")
    public ChegadaResumoDTO entrada(@PathVariable Long id) {
        return service.confirmarEntrada(id);
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
