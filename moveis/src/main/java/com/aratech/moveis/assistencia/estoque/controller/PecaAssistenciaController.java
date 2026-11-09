package com.aratech.moveis.assistencia.estoque.controller;

import com.aratech.moveis.assistencia.estoque.dto.MovimentoAssistenciaDTO;
import com.aratech.moveis.assistencia.estoque.dto.PecaAssistenciaAtualizacaoDTO;
import com.aratech.moveis.assistencia.estoque.dto.PecaAssistenciaDTO;
import com.aratech.moveis.assistencia.estoque.dto.Response;
import com.aratech.moveis.assistencia.estoque.service.PecaAssistenciaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Assistência — Estoque de peças",
        description =
                "Peças incluem modelo, localização, unidade e quantidade mínima. Unidades: un, kg,"
                    + " L, m, cx ou par; quantidades com até 3 casas decimais. Filtro abaixoMinimo"
                    + " seleciona itens no mínimo configurado ou abaixo dele. Entradas e saídas"
                    + " usam chaveOperacao, rejeitam saldo negativo e preservam histórico."
                    + " Arquivamento exige saldo zero.")
@RestController
@RequestMapping("/aratech/assistencia/estoque-pecas")
@RequiredArgsConstructor
public class PecaAssistenciaController {
    private final PecaAssistenciaService service;

    @Operation(
            summary = "Listar peças de assistência",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "false") boolean abaixoMinimo) {
        return service.listar(pagina, tamanho, busca, abaixoMinimo);
    }

    @Operation(
            summary = "Cadastrar peça de assistência",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> adicionar(@RequestBody PecaAssistenciaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionar(dto));
    }

    @Operation(
            summary = "Consultar peça de assistência por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(
            summary = "Atualizar peça de assistência",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping("/{id}")
    public Response atualizar(
            @PathVariable Long id, @RequestBody PecaAssistenciaAtualizacaoDTO dto) {
        return service.atualizar(id, dto);
    }

    @Operation(
            summary = "Arquivar peça de assistência",
            description =
                    "Arquiva sem excluir o histórico. Estoque deve estar zerado antes do"
                            + " arquivamento.")
    @DeleteMapping("/{id}")
    public Response arquivar(@PathVariable Long id) {
        return service.arquivar(id);
    }

    @Operation(
            summary = "Registrar entrada de estoque",
            description =
                    "Altera saldo por movimentação transacional. Envie quantidade positiva, motivo"
                        + " quando exigido e chaveOperacao UUID; a mesma chave e conteúdo não são"
                        + " contabilizados novamente. Saída não pode exceder o saldo disponível.")
    @PostMapping("/{id}/entradas")
    public Response entrada(@PathVariable Long id, @RequestBody MovimentoAssistenciaDTO dto) {
        return service.entrada(id, dto);
    }

    @Operation(
            summary = "Registrar saída de estoque",
            description =
                    "Altera saldo por movimentação transacional. Envie quantidade positiva, motivo"
                        + " quando exigido e chaveOperacao UUID; a mesma chave e conteúdo não são"
                        + " contabilizados novamente. Saída não pode exceder o saldo disponível.")
    @PostMapping("/{id}/saidas")
    public Response saida(@PathVariable Long id, @RequestBody MovimentoAssistenciaDTO dto) {
        return service.saida(id, dto);
    }

    @Operation(
            summary = "Consultar histórico de peça de assistência",
            description =
                    "Retorna o histórico persistido, preservando operações anteriores. Veja os"
                            + " filtros e a paginação disponíveis.")
    @GetMapping("/{id}/movimentacoes")
    public Response historico(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho) {
        return service.historico(id, pagina, tamanho);
    }
}
