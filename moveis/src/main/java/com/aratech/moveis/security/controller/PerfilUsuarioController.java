package com.aratech.moveis.security.controller;

import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.service.PerfilUsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(
        name = "Minha conta — Perfil",
        description =
                "Dados vêm da sessão e do cadastro vinculado ao usuário autenticado, sem aceitar ID"
                    + " de outro funcionário. A conta técnica administrativa não possui vínculo de"
                    + " funcionário. Não exige permissão de consultar o RH. O acessoAtual"
                    + " representa o início da sessão atual, não um histórico de logins.")
@RestController
@RequiredArgsConstructor
public class PerfilUsuarioController {
    private final PerfilUsuarioService service;

    @Operation(
            summary = "Consultar meus dados de perfil",
            description =
                    "Retorna os dados reais do usuário autenticado: identificação, contato, vínculo"
                        + " de trabalho quando existente, perfis e acesso atual. Não consulta outro"
                        + " usuário nem expõe salário, documentos ou dados bancários.")
    @GetMapping("/aratech/auth/meu-perfil")
    public Map<String, Object> meuPerfil(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal
                    UsuarioSessao usuario) {
        return service.meuPerfil(usuario);
    }
}
