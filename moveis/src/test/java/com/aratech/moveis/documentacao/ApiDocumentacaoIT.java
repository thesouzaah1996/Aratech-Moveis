package com.aratech.moveis.documentacao;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.*;

@SpringBootTest(
        properties = {"springdoc.api-docs.enabled=true", "springdoc.swagger-ui.enabled=true"})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ApiDocumentacaoIT {
    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping mapping;

    @Test
    void especificacaoCobreTodosOsControllersComRegrasESeguranca() throws Exception {
        var resposta = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        var doc =
                mapper.readTree(
                        resposta.getResponse()
                                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        assertTrue(doc.path("info").path("title").asText().contains("Aratech"));
        var paths = doc.path("paths");
        int[] quantidade = {0};
        mapping.getHandlerMethods()
                .forEach(
                        (info, handler) -> {
                            if (!handler.getBeanType()
                                    .getPackageName()
                                    .startsWith("com.aratech.moveis")) return;
                            for (String path : info.getPatternValues())
                                for (var metodo : info.getMethodsCondition().getMethods()) {
                                    String verbo = metodo.name().toLowerCase(Locale.ROOT);
                                    var op = paths.path(path).path(verbo);
                                    assertFalse(
                                            op.isMissingNode(),
                                            "Endpoint ausente: " + metodo + " " + path);
                                    assertFalse(op.path("summary").asText().isBlank(), path);
                                    assertFalse(op.path("description").asText().isBlank(), path);
                                    assertFalse(op.path("tags").isEmpty(), path);
                                    assertTrue(op.has("security"), path);
                                    assertTrue(op.has("x-permissao"), path);
                                    assertTrue(op.path("responses").has("403"), path);
                                    if (!Set.of("GET", "HEAD", "OPTIONS").contains(metodo.name()))
                                        assertTrue(
                                                op.path("security").toString().contains("CSRF"),
                                                path);
                                    quantidade[0]++;
                                }
                        });
        assertEquals(255, quantidade[0], "Atualize a cobertura ao adicionar endpoints.");
        assertTrue(paths.has("/aratech/auth/entrar"));
        assertTrue(paths.has("/aratech/auth/sair"));
        assertEquals("EMBALAGEM_ESTEIRAS.FINALIZAR",paths.path("/aratech/embalagem/esteiras/{id}/etapas/{etapa}/concluir").path("post").path("x-permissao").asText());
        assertEquals("EMBALAGEM_LOTES.CONSULTAR",paths.path("/aratech/embalagem/lotes/esteiras").path("get").path("x-permissao").asText());
        assertFalse(paths.has("/aratech/administracao/lotes"));
        assertEquals("PCP_LOTES.CONSULTAR",paths.path("/aratech/pcp/lotes").path("get").path("x-permissao").asText());
        assertEquals("CORTE_LOTES.FINALIZAR",paths.path("/aratech/corte/lotes/{id}/finalizar").path("post").path("x-permissao").asText());
        assertEquals("ADMINISTRACAO_GUARDA_ROUPAS.ALTERAR",paths.path("/aratech/administracao/guarda-roupas/{id}/fotos").path("post").path("x-permissao").asText());
        var marcacao = doc.path("components").path("schemas").path("com.aratech.moveis.rh.ponto.dto.MarcacaoPontoDTO").path("properties");
        assertTrue(marcacao.has("data"));
        assertTrue(marcacao.has("versao"));
        assertFalse(marcacao.has("id"));
        assertFalse(marcacao.has("email"));
        assertFalse(marcacao.has("senha"));
        assertTrue(
                paths.path("/aratech/auth/entrar")
                        .path("post")
                        .path("requestBody")
                        .path("content")
                        .has("application/x-www-form-urlencoded"));
        assertEquals(
                "CARREGAMENTO_ASSISTENCIA.FINALIZAR",
                paths.path("/aratech/assistencia/lotes/{id}/carregar")
                        .path("post")
                        .path("x-permissao")
                        .asText());
        assertEquals(
                "PCP_ORDENS.FINALIZAR",
                paths.path("/aratech/pcp/ordens/{id}/concluir")
                        .path("post")
                        .path("x-permissao")
                        .asText());
        assertEquals(
                "COMERCIAL_PEDIDOS.FINALIZAR",
                paths.path("/aratech/comercial/pedidos/{id}/confirmar")
                        .path("post")
                        .path("x-permissao")
                        .asText());
        assertTrue(
                paths.path("/aratech/rh/funcionarios").path("post").path("responses").has("201"));
        assertEquals("BLOQUEADO", paths.path("/bucket").path("post").path("x-permissao").asText());
        verificarReferencias(doc, doc);
    }

    private void verificarReferencias(JsonNode node, JsonNode raiz) {
        if (node.isObject()) {
            var ref = node.get("$ref");
            if (ref != null && ref.asText().startsWith("#/components/"))
                assertFalse(
                        raiz.at(ref.asText().substring(1)).isMissingNode(),
                        "Referência inválida: " + ref.asText());
            node.properties().forEach(e -> verificarReferencias(e.getValue(), raiz));
        } else if (node.isArray()) for (var item : node) verificarReferencias(item, raiz);
    }

    @Test
    void interfacePersonalizadaEAssetsLocaisEstaoDisponiveis() throws Exception {
        mvc.perform(get("/documentacao/index.html"))
                .andExpect(status().isOk())
                .andExpect(
                        result ->
                                assertTrue(
                                        result.getResponse()
                                                .getContentAsString(
                                                        java.nio.charset.StandardCharsets.UTF_8)
                                                .contains("Aratech | Documentação")))
                .andExpect(
                        header().string(
                                        "Content-Security-Policy",
                                        org.hamcrest.Matchers.containsString("script-src 'self'")));
        mvc.perform(get("/documentacao/aratech.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("X-XSRF-TOKEN")));
        mvc.perform(get("/swagger-ui/swagger-ui-bundle.js")).andExpect(status().isOk());
        mvc.perform(get("/aratech/rh/funcionarios")).andExpect(status().isUnauthorized());
        mvc.perform(post("/aratech/auth/entrar")).andExpect(status().isForbidden());
        mvc.perform(post("/bucket")).andExpect(status().isForbidden());
    }
}
