package com.aratech.moveis.shared.auditoria.config;

import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import jakarta.servlet.http.*;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class AuditoriaLegada implements WebMvcConfigurer {
    private final OperacaoService base;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                        new HandlerInterceptor() {
                            @Override
                            public void afterCompletion(
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object handler,
                                    Exception ex) {
                                if (!request.getRequestURI().startsWith("/aratech/")
                                        || Set.of("GET", "HEAD", "OPTIONS")
                                                .contains(request.getMethod())
                                        || response.getStatus() < 200
                                        || response.getStatus() >= 300
                                        || !(handler instanceof HandlerMethod h)
                                        || h.getBeanType()
                                                .isAnnotationPresent(AuditadoNoServico.class))
                                    return;
                                var auth = SecurityContextHolder.getContext().getAuthentication();
                                if (auth == null || !(auth.getPrincipal() instanceof UsuarioSessao))
                                    return;
                                String permissao = Permissoes.necessaria(request);
                                if (permissao == null) return;
                                try {
                                    base.auditar(
                                            permissao.split("\\.")[0],
                                            request.getMethod(),
                                            "REQUISICAO",
                                            null,
                                            request.getRequestURI());
                                } catch (org.springframework.dao.DataAccessException e) {
                                    org.slf4j.LoggerFactory.getLogger(AuditoriaLegada.class)
                                            .error(
                                                    "Não foi possível registrar metadados da"
                                                            + " operação {}",
                                                    request.getRequestURI(),
                                                    e);
                                }
                            }
                        })
                .addPathPatterns("/aratech/**");
    }
}
