package com.aratech.moveis.documentacao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/** Cadeia exclusiva de leitura da documentação; não altera a segurança da API. */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true")
public class SegurancaDocumentacao {
    @Bean
    @Order(0)
    public SecurityFilterChain documentacaoSecurity(HttpSecurity http) throws Exception {
        return http.securityMatcher(
                        "/documentacao/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml")
                .authorizeHttpRequests(
                        a ->
                                a.requestMatchers(HttpMethod.GET, "/**")
                                        .permitAll()
                                        .anyRequest()
                                        .denyAll())
                .requestCache(AbstractHttpConfigurer::disable)
                .headers(
                        h ->
                                h.contentSecurityPolicy(
                                        c ->
                                                c.policyDirectives(
                                                        "default-src 'self'; script-src 'self';"
                                                            + " style-src 'self' 'unsafe-inline';"
                                                            + " img-src 'self' data:; connect-src"
                                                            + " 'self'; frame-ancestors 'none';"
                                                            + " base-uri 'none'; form-action"
                                                            + " 'self'")))
                .build();
    }
}
