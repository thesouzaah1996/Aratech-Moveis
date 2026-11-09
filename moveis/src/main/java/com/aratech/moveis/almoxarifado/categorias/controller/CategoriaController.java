package com.aratech.moveis.almoxarifado.categorias.controller;

import com.aratech.moveis.almoxarifado.categorias.dto.CategoriaDTO;
import com.aratech.moveis.almoxarifado.categorias.dto.Response;
import com.aratech.moveis.almoxarifado.categorias.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Almoxarifado — Categorias",
        description =
                "Nomes são normalizados e validados. Cadastros têm estado ativo/inativo; alterações"
                    + " e desativações preservam o registro. Duplicidades são rejeitadas. Filtros"
                    + " opcionais por nome e situação.")
@RestController
@RequestMapping("/aratech/almoxarifado/categorias")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaService service;

    @Operation(
            summary = "Cadastrar categoria",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addCategoria(@RequestBody CategoriaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addCategoria(dto));
    }

    @Operation(
            summary = "Listar categorias",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response getAllCategorias(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo) {
        if ((nome == null || nome.isBlank()) && ativo == null)
            return service.getAllCategorias(pagina, tamanho);
        return service.getAllCategorias(pagina, tamanho, nome, ativo);
    }

    @Operation(
            summary = "Consultar categoria por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response getCategoriaById(@PathVariable Long id) {
        return service.getCategoriaById(id);
    }

    @Operation(
            summary = "Atualizar categoria",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response updateCategoria(@PathVariable Long id, @RequestBody CategoriaDTO dto) {
        return service.updateCategoria(dto, id);
    }

    @Operation(
            summary = "Ativar categoria",
            description = "Ativa o cadastro existente, respeitando as regras e vínculos do módulo.")
    @PatchMapping("/{id}/ativar")
    public Response enableCategoria(@PathVariable Long id) {
        return service.enableCategoria(id);
    }

    @Operation(
            summary = "Desativar categoria",
            description =
                    "Desativa sem excluir o histórico e respeita as regras de vínculos do módulo.")
    @PatchMapping("/{id}/desativar")
    public Response disableCategoria(@PathVariable Long id) {
        return service.disableCategoria(id);
    }
}
