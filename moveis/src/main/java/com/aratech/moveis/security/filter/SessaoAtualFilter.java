package com.aratech.moveis.security.filter;

import com.aratech.moveis.security.authorization.SessaoAuthorities;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.handler.SecurityJson;
import com.aratech.moveis.security.service.ContasService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.*;

public class SessaoAtualFilter extends OncePerRequestFilter {
    private final ContasService contas;
    private final SecurityJson json;

    public SessaoAtualFilter(ContasService contas, SecurityJson json) {
        this.contas = contas;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioSessao anterior) {
            try {
                var atual = contas.carregar(anterior.id(), anterior.autenticadoEm());
                if (atual.versao() != anterior.versao()
                        || !atual.email().equalsIgnoreCase(anterior.email())
                        || anterior.autenticadoEm()
                                .plus(Duration.ofHours(8))
                                .isBefore(Instant.now()))
                    throw new org.springframework.security.authentication
                            .CredentialsExpiredException("Sessão expirada.");
                // Cada requisição usa seu próprio contexto; requisições paralelas não sobrescrevem
                // as permissões umas das outras.
                var contexto = SecurityContextHolder.createEmptyContext();
                contexto.setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                atual, null, SessaoAuthorities.authorities(atual)));
                SecurityContextHolder.setContext(contexto);
            } catch (AuthenticationException e) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            } catch (org.springframework.dao.DataAccessException e) {
                json.erro(
                        response,
                        503,
                        "SERVICO_INDISPONIVEL",
                        "Não foi possível verificar a sessão. Tente novamente.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
