package com.aratech.moveis.security.handler;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SecurityJson {
    private final JsonMapper mapper;

    public void escrever(HttpServletResponse response, int status, Object corpo)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getOutputStream(), corpo);
    }

    public void erro(HttpServletResponse response, int status, String codigo, String mensagem)
            throws IOException {
        escrever(
                response, status, Map.of("status", status, "codigo", codigo, "mensagem", mensagem));
    }
}
