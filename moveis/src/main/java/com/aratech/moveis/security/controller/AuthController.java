package com.aratech.moveis.security.controller;

import com.aratech.moveis.security.dto.Alterar;
import com.aratech.moveis.security.dto.Sessao;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.service.SenhasService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(
        name = "Autenticação",
        description =
                "Autenticação por cookie ARATECHSESSION HttpOnly; não utiliza JWT/Bearer. Escritas"
                    + " exigem X-XSRF-TOKEN, inclusive login e logout. Obtenha o token em GET"
                    + " /aratech/auth/csrf. Sessão inativa expira em 30 minutos, com limite"
                    + " absoluto de 8 horas. Login troca o ID da sessão. Conta inativa é rejeitada."
                    + " Senha temporária permite somente sessão, saída e troca de senha. Não há"
                    + " recuperação de senha por e-mail.")
@RestController
@RequestMapping("/aratech/auth")
@RequiredArgsConstructor
public class AuthController {
    private final SenhasService senhas;

    public static Sessao resumo(UsuarioSessao u) {
        return new Sessao(
                u.id(),
                u.nome(),
                u.email(),
                u.administrador(),
                u.perfis(),
                u.permissoes(),
                u.trocarSenha());
    }

    @Operation(
            summary = "Obter token CSRF",
            description =
                    "Público. Inicia ou reutiliza a sessão de segurança e devolve headerName e"
                            + " token. O token pertence ao cookie da sessão; obtenha novamente após"
                            + " login ou troca de senha.")
    @GetMapping("/csrf")
    public Map<String, String> csrf(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) CsrfToken csrf) {
        return Map.of("headerName", csrf.getHeaderName(), "token", csrf.getToken());
    }

    @Operation(
            summary = "Consultar sessão atual",
            description =
                    "Retorna identidade, perfis, permissões e trocarSenha. Não retorna hash nem"
                            + " senha. Aceita sessão com senha temporária.")
    @GetMapping("/sessao")
    public Sessao sessao(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal
                    UsuarioSessao usuario) {
        return resumo(usuario);
    }

    @Operation(
            summary = "Trocar senha autenticada",
            description =
                    "Informe senhaAtual e novaSenha diferentes. Nova senha: 8 a 128 caracteres,"
                        + " avaliada localmente por zxcvbn; combinações previsíveis são rejeitadas."
                        + " Até 10 tentativas de troca por janela de 15 minutos. A troca elimina a"
                        + " exigência de senha temporária, incrementa a versão e revoga sessões"
                        + " anteriores; faça novo login.")
    @PostMapping("/senha")
    public Map<String, String> alterar(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal
                    UsuarioSessao usuario,
            @RequestBody Alterar dto) {
        senhas.alterar(usuario, dto.senhaAtual(), dto.novaSenha());
        return Map.of("mensagem", "Senha alterada. Entre novamente.");
    }
}
