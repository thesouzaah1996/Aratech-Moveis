package com.aratech.moveis.manutencao.mecanicos.controller;

import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.mecanicos.dto.MecanicoDTO;
import com.aratech.moveis.manutencao.mecanicos.service.MecanicoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Manutenção — Mecânicos",
        description =
                "Cadastro e edição validam os dados do mecânico. Ativação/desativação preservam o"
                    + " registro. Somente mecânicos ativos podem ser atribuídos a chamados. Lista"
                    + " paginada com filtro opcional ativo.")
@RestController
@RequestMapping("/aratech/manutencao/mecanicos")
@RequiredArgsConstructor
public class MecanicoController {
    private final MecanicoService service;

    @Operation(
            summary = "Listar mecânicos",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(required = false) Boolean ativo) {
        return service.listar(pagina, tamanho, ativo);
    }

    @Operation(
            summary = "Cadastrar mecânico",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping
    public ResponseEntity<Response> criar(@RequestBody MecanicoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(null, dto));
    }

    @Operation(
            summary = "Atualizar mecânico",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping("/{id}")
    public Response editar(@PathVariable Long id, @RequestBody MecanicoDTO dto) {
        return service.salvar(id, dto);
    }

    @Operation(
            summary = "Ativar mecânico",
            description = "Ativa o cadastro existente, respeitando as regras e vínculos do módulo.")
    @PatchMapping("/{id}/ativar")
    public Response ativar(@PathVariable Long id) {
        return service.status(id, true);
    }

    @Operation(
            summary = "Desativar mecânico",
            description =
                    "Desativa sem excluir o histórico e respeita as regras de vínculos do módulo.")
    @PatchMapping("/{id}/desativar")
    public Response desativar(@PathVariable Long id) {
        return service.status(id, false);
    }
}
