package com.aratech.moveis.almoxarifado.produtos.controller;

import com.aratech.moveis.almoxarifado.produtos.dto.MovimentacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.ProdutoAtualizacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.ProdutoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.Response;
import com.aratech.moveis.almoxarifado.produtos.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Almoxarifado — Produtos",
        description =
                "SKU único, inclusive entre arquivados, e imutável após o cadastro. Categoria e"
                    + " fornecedor devem existir e estar ativos. Quantidades são inteiras; saldo"
                    + " entre zero e 1 bilhão. Validade nova deve ser futura. Saldo é alterado"
                    + " somente por movimentações, com histórico. Arquivamento exige saldo zero. A"
                    + " chaveOperacao evita duplicação de entradas e saídas; reutilização com dados"
                    + " diferentes retorna conflito. Quantidade mínima opcional é configurada por"
                    + " PATCH /{id}/minimo. Em saldo <= mínimo, gera pedido imediato de reposição"
                    + " em Compras, sem duplicar o episódio. Saldo acima do mínimo cancela pedido"
                    + " automático ainda pendente; aprovação não altera estoque.")
@RestController
@RequestMapping("/aratech/almoxarifado/produtos")
@RequiredArgsConstructor
public class ProdutoController {
    private final ProdutoService service;

    @Operation(
            summary = "Cadastrar produto",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addProduto(@RequestBody ProdutoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addProduto(dto));
    }

    @Operation(
            summary = "Listar produtos",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response getAllProdutos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @RequestParam(required = false) String busca) {
        return service.getAllProdutos(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Consultar produto por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response getProdutoById(@PathVariable Long id) {
        return service.getProdutoById(id);
    }

    @Operation(
            summary = "Atualizar produto",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response updateProduto(@PathVariable Long id, @RequestBody ProdutoAtualizacaoDTO dto) {
        return service.updateProduto(dto, id);
    }

    @Operation(
            summary = "Arquivar produto",
            description =
                    "Arquiva sem excluir o histórico. Estoque deve estar zerado antes do"
                            + " arquivamento.")
    @DeleteMapping("/{id}")
    public Response deleteProduto(@PathVariable Long id) {
        return service.deleteProduto(id);
    }

    @Operation(
            summary = "Registrar entrada de estoque",
            description =
                    "Altera saldo por movimentação transacional. Envie quantidade positiva, motivo"
                        + " quando exigido e chaveOperacao UUID; a mesma chave e conteúdo não são"
                        + " contabilizados novamente. Saída não pode exceder o saldo disponível.")
    @PostMapping(value = "/{id}/entradas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response entrada(@PathVariable Long id, @RequestBody MovimentacaoDTO dto) {
        return service.entrada(id, dto);
    }

    @Operation(
            summary = "Registrar saída de estoque",
            description =
                    "Altera saldo por movimentação transacional. Envie quantidade positiva, motivo"
                        + " quando exigido e chaveOperacao UUID; a mesma chave e conteúdo não são"
                        + " contabilizados novamente. Saída não pode exceder o saldo disponível.")
    @PostMapping(value = "/{id}/saidas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response saida(@PathVariable Long id, @RequestBody MovimentacaoDTO dto) {
        return service.saida(id, dto);
    }

    @Operation(
            summary = "Consultar histórico de produto",
            description =
                    "Retorna o histórico persistido, preservando operações anteriores. Veja os"
                            + " filtros e a paginação disponíveis.")
    @GetMapping("/{id}/movimentacoes")
    public Response getMovimentacoes(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.getMovimentacoes(id, pagina, tamanho);
    }
}
