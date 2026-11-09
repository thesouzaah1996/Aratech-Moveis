package com.aratech.moveis.rh.perfis.controller;

import com.aratech.moveis.rh.perfis.dto.PerfilDTO;
import com.aratech.moveis.rh.perfis.dto.PerfilResumoDTO;
import com.aratech.moveis.rh.perfis.service.PerfilService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "RH — Perfis de acesso",
        description =
                "Cria e renomeia perfis existentes. Nomes têm 3 a 100 caracteres e são"
                    + " normalizados. Criar um perfil chamado ADMIN ou Administrador não concede"
                    + " privilégios de administrador. Todas as operações exigem ACESSOS.GERENCIAR;"
                    + " as permissões efetivas são definidas na gestão de acessos.")
@RestController
@RequestMapping("/aratech/rh/perfis")
@RequiredArgsConstructor
public class PerfilController {
    private final PerfilService service;

    @Operation(
            summary = "Listar perfis de acesso",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Page<PerfilResumoDTO> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(required = false) String busca) {
        return service.listar(pagina, tamanho, busca);
    }

    @Operation(
            summary = "Cadastrar perfil de acesso",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilResumoDTO criar(@RequestBody PerfilDTO dto) {
        return service.criar(dto);
    }

    @Operation(
            summary = "Atualizar perfil de acesso",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                            + " do registro são validados segundo as regras do módulo.")
    @PutMapping("/{id}")
    public PerfilResumoDTO atualizar(@PathVariable Long id, @RequestBody PerfilDTO dto) {
        return service.atualizar(id, dto);
    }
}
