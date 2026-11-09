package com.aratech.moveis.almoxarifado.fornecedores.controller;

import com.aratech.moveis.almoxarifado.fornecedores.dto.FornecedorDTO;
import com.aratech.moveis.almoxarifado.fornecedores.dto.Response;
import com.aratech.moveis.almoxarifado.fornecedores.service.FornecedorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Almoxarifado — Fornecedores",
        description =
                "Nome e e-mail devem ser únicos, sem distinção entre maiúsculas e minúsculas."
                        + " E-mails são normalizados. O representante é informado no objeto"
                        + " representante. Desativação preserva o cadastro; novas vinculações de"
                        + " produtos exigem fornecedor ativo.")
@RestController
@RequestMapping("/aratech/almoxarifado/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {
    private final FornecedorService service;

    @Operation(
            summary = "Cadastrar fornecedor",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addFornecedor(@RequestBody FornecedorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addFornecedor(dto));
    }

    @Operation(
            summary = "Listar fornecedores",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response getAllFornecedores(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo) {
        if ((busca == null || busca.isBlank()) && ativo == null)
            return service.getAllFornecedores(pagina, tamanho);
        return service.getAllFornecedores(pagina, tamanho, busca, ativo);
    }

    @Operation(
            summary = "Consultar fornecedor por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response getFornecedorById(@PathVariable Long id) {
        return service.getFornecedorById(id);
    }

    @Operation(
            summary = "Atualizar fornecedor",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response updateFornecedor(@PathVariable Long id, @RequestBody FornecedorDTO dto) {
        return service.updateFornecedor(dto, id);
    }

    @Operation(
            summary = "Ativar fornecedor",
            description = "Ativa o cadastro existente, respeitando as regras e vínculos do módulo.")
    @PatchMapping("/{id}/ativar")
    public Response enableFornecedor(@PathVariable Long id) {
        return service.enableFornecedor(id);
    }

    @Operation(
            summary = "Desativar fornecedor",
            description =
                    "Desativa sem excluir o histórico e respeita as regras de vínculos do módulo.")
    @PatchMapping("/{id}/desativar")
    public Response disableFornecedor(@PathVariable Long id) {
        return service.disableFornecedor(id);
    }
}
