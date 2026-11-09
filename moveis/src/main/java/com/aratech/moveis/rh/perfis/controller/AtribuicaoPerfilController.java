package com.aratech.moveis.rh.perfis.controller;

import com.aratech.moveis.rh.perfis.dto.Atribuicao;
import com.aratech.moveis.rh.perfis.dto.UsuarioPerfilDTO;
import com.aratech.moveis.rh.perfis.service.AtribuicaoPerfilService;
import com.aratech.moveis.security.service.GestaoAcessoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "RH — Atribuição de perfis",
        description =
                "Busca por nome ou prefixo do ID do funcionário. A atribuição recebe o nome de um"
                    + " perfil já cadastrado, valida existência e preserva as demais atribuições."
                    + " Repetir o mesmo vínculo não duplica. Gestores não podem conceder permissões"
                    + " superiores às próprias.")
@RestController
@RequestMapping("/aratech/rh/perfis/usuarios")
@RequiredArgsConstructor
public class AtribuicaoPerfilController {
    private final AtribuicaoPerfilService service;
    private final GestaoAcessoService gestao;

    @Operation(
            summary = "Buscar usuários por nome ou ID",
            description =
                    "Busca textual de nome ou prefixo numérico do ID. Resultados paginados incluem"
                            + " perfis já atribuídos; não atribui perfil automaticamente.")
    @GetMapping
    public Page<UsuarioPerfilDTO> buscar(
            @RequestParam(defaultValue = "") String busca,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho) {
        return service.buscar(busca, pagina, tamanho);
    }

    @org.springframework.transaction.annotation.Transactional
    @Operation(
            summary = "Atribuir perfil existente por nome",
            description =
                    "Recebe {nome}. Confere existência do perfil, impede concessão acima do gestor"
                            + " e preserva os vínculos anteriores.")
    @PostMapping("/{id}/atribuicoes")
    public UsuarioPerfilDTO atribuir(@PathVariable Long id, @RequestBody Atribuicao dto) {
        gestao.validarPerfilPorNome(dto == null ? null : dto.nome());
        return service.atribuir(id, dto == null ? null : dto.nome());
    }
}
