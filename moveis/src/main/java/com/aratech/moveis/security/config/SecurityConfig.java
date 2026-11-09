package com.aratech.moveis.security.config;

import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.controller.AuthController;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.ExcessoTentativas;
import com.aratech.moveis.security.filter.SessaoAtualFilter;
import com.aratech.moveis.security.handler.SecurityJson;
import com.aratech.moveis.security.provider.LoginProvider;
import com.aratech.moveis.security.service.ContasService;

import jakarta.servlet.DispatcherType;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.*;

import java.util.Map;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        var encoder =
                new Pbkdf2PasswordEncoder(
                        "",
                        16,
                        600000,
                        Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        return new DelegatingPasswordEncoder("pbkdf2-600k", Map.of("pbkdf2-600k", encoder));
    }

    @Bean
    public AuthenticationManager authenticationManager(LoginProvider provider) {
        return new ProviderManager(provider);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            ContasService contas,
            SecurityJson json)
            throws Exception {
        var csrf = new HttpSessionCsrfTokenRepository();
        csrf.setHeaderName("X-XSRF-TOKEN");
        http.authenticationManager(authenticationManager)
                .csrf(
                        c ->
                                c.csrfTokenRepository(csrf)
                                        .csrfTokenRequestHandler(
                                                new CsrfTokenRequestAttributeHandler()))
                .sessionManagement(
                        s ->
                                s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                        .sessionFixation(f -> f.changeSessionId()))
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAfter(
                        new SessaoAtualFilter(contas, json), SecurityContextHolderFilter.class)
                .exceptionHandling(
                        e ->
                                e.authenticationEntryPoint(
                                                (req, res, ex) ->
                                                        json.erro(
                                                                res,
                                                                401,
                                                                "NAO_AUTENTICADO",
                                                                "Entre na aplicação para"
                                                                        + " continuar."))
                                        .accessDeniedHandler(
                                                (req, res, ex) ->
                                                        json.erro(
                                                                res,
                                                                403,
                                                                ex instanceof CsrfException
                                                                        ? "CSRF_INVALIDO"
                                                                        : "SEM_PERMISSAO",
                                                                ex instanceof CsrfException
                                                                        ? "A sessão de segurança"
                                                                                + " expirou. Tente"
                                                                                + " novamente."
                                                                        : "Você não possui"
                                                                              + " permissão para"
                                                                              + " esta operação.")))
                .authorizeHttpRequests(
                        a ->
                                a.dispatcherTypeMatchers(DispatcherType.ERROR)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/aratech/auth/csrf")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/aratech/auth/entrar")
                                        .permitAll()
                                        .requestMatchers(
                                                "/aratech/auth/sessao", "/aratech/auth/senha")
                                        .authenticated()
                                        .requestMatchers("/aratech/**")
                                        .access(
                                                (authentication, contexto) -> {
                                                    Authentication auth = authentication.get();
                                                    if (auth == null
                                                            || !(auth.getPrincipal()
                                                                    instanceof
                                                                    UsuarioSessao usuario))
                                                        return new AuthorizationDecision(false);
                                                    if (usuario.trocarSenha())
                                                        return new AuthorizationDecision(false);
                                                    String caminho =
                                                            Permissoes.caminho(
                                                                    contexto.getRequest());
                                                    if (contexto.getRequest().getMethod().equals("POST")
                                                            && caminho.equals("/aratech/auth/meu-ponto/marcacoes"))
                                                        return new AuthorizationDecision(true);
                                                    if (contexto.getRequest()
                                                                    .getMethod()
                                                                    .equals("GET")
                                                            && (caminho.equals(
                                                                            "/aratech/auth/meu-perfil")
                                                                    || caminho.equals(
                                                                            "/aratech/auth/meu-ponto")
                                                                    || caminho.equals("/aratech/auth/meu-ponto/relogio")
                                                                    || caminho.matches(
                                                                            "/aratech/auth/meu-ponto/[0-9-]+/historico")))
                                                        return new AuthorizationDecision(true);
                                                    String permissao =
                                                            Permissoes.necessaria(
                                                                    contexto.getRequest());
                                                    boolean permitido =
                                                            usuario.administrador()
                                                                    || Permissoes.permite(
                                                                            usuario, permissao);
                                                    if (contexto.getRequest()
                                                                    .getMethod()
                                                                    .equals("GET")
                                                            && Permissoes.caminho(
                                                                            contexto.getRequest())
                                                                    .equals(
                                                                            "/aratech/assistencia/lotes"))
                                                        permitido =
                                                                permitido
                                                                        || Permissoes.permite(
                                                                                usuario,
                                                                                "CARREGAMENTO_ASSISTENCIA.CONSULTAR");
                                                    return new AuthorizationDecision(permitido);
                                                })
                                        .anyRequest()
                                        .denyAll())
                .formLogin(
                        f ->
                                f.loginPage("/login")
                                        .loginProcessingUrl("/aratech/auth/entrar")
                                        .usernameParameter("emailCorporativo")
                                        .passwordParameter("senha")
                                        .successHandler(
                                                (req, res, auth) ->
                                                        json.escrever(
                                                                res,
                                                                200,
                                                                AuthController.resumo(
                                                                        (UsuarioSessao)
                                                                                auth
                                                                                        .getPrincipal())))
                                        .failureHandler(
                                                (req, res, ex) -> {
                                                    boolean limite =
                                                            ex instanceof ExcessoTentativas;
                                                    if (limite) res.setHeader("Retry-After", "900");
                                                    json.erro(
                                                            res,
                                                            limite ? 429 : 401,
                                                            "LOGIN_INVALIDO",
                                                            limite
                                                                    ? "Não foi possível entrar."
                                                                            + " Aguarde e tente"
                                                                            + " novamente."
                                                                    : "Usuário ou senha"
                                                                            + " incorretos.");
                                                }))
                .logout(
                        l ->
                                l.logoutUrl("/aratech/auth/sair")
                                        .invalidateHttpSession(true)
                                        .clearAuthentication(true)
                                        .deleteCookies("ARATECHSESSION")
                                        .logoutSuccessHandler(
                                                (req, res, auth) ->
                                                        json.escrever(
                                                                res,
                                                                200,
                                                                Map.of(
                                                                        "mensagem",
                                                                        "Sessão encerrada."))))
                .headers(
                        h ->
                                h.contentSecurityPolicy(
                                        c ->
                                                c.policyDirectives(
                                                        "default-src 'none'; frame-ancestors"
                                                                + " 'none'; base-uri 'none'")));
        return http.build();
    }
}
