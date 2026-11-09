package com.aratech.moveis.manutencao.estoque.controller;

import com.aratech.moveis.manutencao.estoque.dto.MovimentacaoPecaDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueAtualizacaoDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueDTO;
import com.aratech.moveis.manutencao.estoque.dto.Response;
import com.aratech.moveis.manutencao.estoque.service.PecaEstoqueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Manutenção — Estoque de peças",
        description =
                "Código identifica a peça. Unidades: un, kg, L, m, cx ou par. Quantidades decimais"
                    + " com até 3 casas. Entrada e saída são transacionais e mantêm histórico;"
                    + " saldo não pode ficar negativo. Movimentações usam chaveOperacao para evitar"
                    + " duplicação. Arquivamento preserva o histórico e exige estoque zerado."
                    + " Quantidade mínima opcional é configurada por PATCH /{id}/minimo, com até"
                    + " três casas decimais. Saldo <= mínimo gera pedido automático em Compras; a"
                    + " decisão e eventual motivo são consultados em /aratech/manutencao/pedidos.")
@RestController
@RequestMapping("/aratech/manutencao/estoque")
@RequiredArgsConstructor
public class PecaEstoqueController {
    private final PecaEstoqueService service;

    @Operation(
            summary = "Listar peças de manutenção",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(required = false) String busca) {
        return service.listar(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Cadastrar peça de manutenção",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> adicionar(@RequestBody PecaEstoqueDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionar(dto));
    }

    @Operation(
            summary = "Consultar peça de manutenção por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(
            summary = "Atualizar peça de manutenção",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping("/{id}")
    public Response atualizar(@PathVariable Long id, @RequestBody PecaEstoqueAtualizacaoDTO dto) {
        return service.atualizar(id, dto);
    }

    @Operation(
            summary = "Arquivar peça de manutenção",
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
    public Response entrada(@PathVariable Long id, @RequestBody MovimentacaoPecaDTO dto) {
        return service.entrada(id, dto);
    }

    @Operation(
            summary = "Registrar saída de estoque",
            description =
                    "Altera saldo por movimentação transacional. Envie quantidade positiva, motivo"
                        + " quando exigido e chaveOperacao UUID; a mesma chave e conteúdo não são"
                        + " contabilizados novamente. Saída não pode exceder o saldo disponível.")
    @PostMapping("/{id}/saidas")
    public Response saida(@PathVariable Long id, @RequestBody MovimentacaoPecaDTO dto) {
        return service.saida(id, dto);
    }

    @Operation(
            summary = "Consultar histórico de peça de manutenção",
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
