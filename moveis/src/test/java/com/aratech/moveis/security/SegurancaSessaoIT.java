package com.aratech.moveis.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.security.authorization.SessaoAuthorities;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.security.service.GestaoAcessoService;
import com.aratech.moveis.security.service.SenhasService;
import com.aratech.moveis.security.validation.IdentificadorHash;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.servlet.session.cookie.secure=true")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class SegurancaSessaoIT {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper mapper;
    @Autowired PasswordEncoder encoder;
    @Autowired SenhasService senhas;
    @Autowired GestaoAcessoService gestao;
    @Autowired PlatformTransactionManager transactionManager;
    @LocalServerPort int port;
    @Autowired jakarta.servlet.ServletContext servletContext;
    final String senha = "Frase de teste segura 2026!";
    final String ipTeste = "192.0.2.42";
    Long id;
    String email;

    @BeforeEach
    void usuario() {
        email = UUID.randomUUID() + "@aratechmoveis";
        limparTentativas();
        id =
                jdbc.queryForObject(
                        "INSERT INTO funcionario(nome_completo,email,setor,ativo) VALUES ('Usuário"
                                + " de segurança',?,'RH',TRUE) RETURNING id",
                        Long.class,
                        email);
        jdbc.update(
                "INSERT INTO acesso_conta(funcionario_id,senha_hash) VALUES (?,?)",
                id,
                encoder.encode(senha));
    }

    @AfterEach
    void limparTentativas() {
        var tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult(
                s ->
                        jdbc.update(
                                "DELETE FROM acesso_tentativa WHERE chave IN (?, ?, ?, ?)",
                                "ip:" + IdentificadorHash.hash(ipTeste),
                                "conta:" + IdentificadorHash.hash(email),
                                "conta:" + IdentificadorHash.hash("inexistente-" + email),
                                "troca-senha:" + IdentificadorHash.hash("tecnico:" + email)));
    }

    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder entrar() {
        return post("/aratech/auth/entrar")
                .with(
                        request -> {
                            request.setRemoteAddr(ipTeste);
                            return request;
                        });
    }

    String csrf(MockHttpSession session) throws Exception {
        var r =
                mvc.perform(get("/aratech/auth/csrf").session(session))
                        .andExpect(status().isOk())
                        .andReturn();
        return mapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    MockHttpSession login() throws Exception {
        var session = new MockHttpSession();
        String antigo = session.getId();
        mvc.perform(
                        entrar().session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .param("emailCorporativo", email)
                                .param("senha", senha))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("nome").value("Usuário de segurança"))
                .andExpect(jsonPath("token").doesNotExist());
        assertNotEquals(antigo, session.getId(), "O login deve trocar o identificador da sessão.");
        return session;
    }

    Long permitir(String... permissoes) {
        Long perfil =
                jdbc.queryForObject(
                        "INSERT INTO perfil_acesso(nome) VALUES (?) RETURNING id",
                        Long.class,
                        "TESTE-" + UUID.randomUUID());
        jdbc.update(
                "INSERT INTO funcionario_perfil_acesso(funcionario_id,perfil_id) VALUES (?,?)",
                id,
                perfil);
        for (String p : permissoes)
            jdbc.update(
                    "INSERT INTO perfil_permissao_acesso(perfil_id,permissao) VALUES (?,?)",
                    perfil,
                    p);
        return perfil;
    }

    @Test
    void exigeLoginECsrfInclusiveParaEntrar() throws Exception {
        mvc.perform(get("/aratech/rh/funcionarios")).andExpect(status().isUnauthorized());
        mvc.perform(entrar().param("emailCorporativo", email).param("senha", senha))
                .andExpect(status().isForbidden());
        mvc.perform(get("/bucket")).andExpect(status().isUnauthorized());
    }

    @Test
    void sessaoPermiteQuinzeMinutosDeInatividade() {
        assertEquals(
                15,
                servletContext.getSessionTimeout(),
                "O contêiner deve aplicar 15 minutos de inatividade.");
    }

    @Test
    void loginTrocaSessaoELogoutInvalidaNoServidor() throws Exception {
        var session = login();
        mvc.perform(get("/aratech/auth/sessao").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("nome").value("Usuário de segurança"))
                .andExpect(jsonPath("email").value(email));
        mvc.perform(
                        post("/aratech/auth/sair")
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session)))
                .andExpect(status().isOk());
        assertTrue(session.isInvalid());
        mvc.perform(get("/aratech/auth/sessao")).andExpect(status().isUnauthorized());
    }

    @Test
    void permissoesSaoExigidasEAtualizadasComAMesmaSessao() throws Exception {
        var session = login();
        mvc.perform(get("/aratech/rh/funcionarios").session(session))
                .andExpect(status().isForbidden());
        Long perfil = permitir("RH.CONSULTAR");
        mvc.perform(get("/aratech/rh/funcionarios").session(session)).andExpect(status().isOk());
        mvc.perform(
                        patch("/aratech/rh/funcionarios/1/desativar")
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session)))
                .andExpect(status().isForbidden());
        jdbc.update("DELETE FROM perfil_permissao_acesso WHERE perfil_id=?", perfil);
        mvc.perform(get("/aratech/rh/funcionarios").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void nomeAdministradorNaoConcedePrivilegios() throws Exception {
        Long perfil = permitir();
        jdbc.update(
                "UPDATE perfil_acesso SET nome=? WHERE id=?",
                "ADMINISTRADOR-" + UUID.randomUUID(),
                perfil);
        var session = login();
        mvc.perform(get("/aratech/auth/usuarios/permissoes").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void desativarEReativarRevogaSessaoAnterior() throws Exception {
        var session = login();
        jdbc.update("UPDATE funcionario SET ativo=FALSE WHERE id=?", id);
        jdbc.update("UPDATE funcionario SET ativo=TRUE WHERE id=?", id);
        mvc.perform(get("/aratech/auth/sessao").session(session))
                .andExpect(status().isUnauthorized());
        login();
    }

    @Test
    void falhaDeLoginNaoRevelaSeContaExisteELimitaTentativas() throws Exception {
        var session = new MockHttpSession();
        String token = csrf(session);
        var primeira =
                mvc.perform(
                                entrar().session(session)
                                        .header("X-XSRF-TOKEN", token)
                                        .param("emailCorporativo", email)
                                        .param("senha", "incorreta"))
                        .andExpect(status().isUnauthorized())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        var inexistente =
                mvc.perform(
                                entrar().session(session)
                                        .header("X-XSRF-TOKEN", token)
                                        .param("emailCorporativo", "inexistente-" + email)
                                        .param("senha", "incorreta"))
                        .andExpect(status().isUnauthorized())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertEquals(primeira, inexistente);
        for (int i = 1; i < 10; i++)
            mvc.perform(
                            entrar().session(session)
                                    .header("X-XSRF-TOKEN", token)
                                    .param("emailCorporativo", email)
                                    .param("senha", "incorreta"))
                    .andExpect(status().isUnauthorized());
        mvc.perform(
                        entrar().session(session)
                                .header("X-XSRF-TOKEN", token)
                                .param("emailCorporativo", email)
                                .param("senha", senha))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void alteracaoDeEmailNaoInvalidaLoginMasPreservaEmailPessoalSeparado() {
        var usuario =
                new UsuarioSessao(id, "Gestor", email, true, 0L, Instant.now(), Set.of(), Set.of());
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(
                        org.springframework.security.authentication
                                .UsernamePasswordAuthenticationToken.authenticated(
                                usuario, null, List.of()));
        try {
            assertEquals(
                    400,
                    assertThrows(
                                    AuthException.class,
                                    () -> gestao.validarMudancaEmail(id, "pessoal@example.com"))
                            .status());
            assertDoesNotThrow(() -> gestao.validarMudancaEmail(id, "novo@aratechmoveis"));
            jdbc.update(
                    "UPDATE acesso_conta SET email_corporativo=? WHERE funcionario_id=?",
                    email,
                    id);
            assertDoesNotThrow(() -> gestao.validarMudancaEmail(id, "pessoal@example.com"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void rejeitaLoginSemDominioMesmoComSenhaCorreta() throws Exception {
        jdbc.update("UPDATE acesso_conta SET email_corporativo='admin' WHERE funcionario_id=?", id);
        var session = new MockHttpSession();
        mvc.perform(
                        entrar().session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .param("emailCorporativo", "admin")
                                .param("senha", senha))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("codigo").value("LOGIN_INVALIDO"));
    }

    @Test
    void rejeitaDominioExternoNaCriacaoSemPersistirCredencial() throws Exception {
        permitir("ACESSOS.GERENCIAR");
        var session = login();
        Long alvo =
                jdbc.queryForObject(
                        "INSERT INTO funcionario(nome_completo,email,ativo) VALUES ('Login inválido"
                                + " teste',?,TRUE) RETURNING id",
                        Long.class,
                        UUID.randomUUID() + "@example.test");
        for (String login : List.of("admin", "admin@example.com", "ana..silva@aratechmoveis"))
            mvc.perform(
                            post("/aratech/auth/usuarios/" + alvo + "/email-corporativo")
                                    .session(session)
                                    .header("X-XSRF-TOKEN", csrf(session))
                                    .contentType("application/json")
                                    .content(
                                            mapper.writeValueAsString(
                                                    Map.of("emailCorporativo", login))))
                    .andExpect(status().isBadRequest());
        assertEquals(
                0L,
                jdbc.queryForObject(
                        "SELECT count(*) FROM acesso_conta WHERE funcionario_id=?",
                        Long.class,
                        alvo));
    }

    @Test
    void emailCorporativoPreservaPessoalExigeTrocaEValidaDuplicidade() throws Exception {
        permitir("ACESSOS.GERENCIAR");
        var gestor = login();
        Long alvo =
                jdbc.queryForObject(
                        "INSERT INTO funcionario(nome_completo,email,ativo) VALUES ('Corporativo"
                                + " teste',?,TRUE) RETURNING id",
                        Long.class,
                        UUID.randomUUID() + "@example.test");
        String corporativo = UUID.randomUUID() + "@aratechmoveis";
        var r =
                mvc.perform(
                                post("/aratech/auth/usuarios/" + alvo + "/email-corporativo")
                                        .session(gestor)
                                        .header("X-XSRF-TOKEN", csrf(gestor))
                                        .contentType("application/json")
                                        .content(
                                                mapper.writeValueAsString(
                                                        Map.of("emailCorporativo", corporativo))))
                        .andExpect(status().isOk())
                        .andExpect(header().string("Cache-Control", "no-store"))
                        .andReturn();
        String temporaria =
                mapper.readTree(r.getResponse().getContentAsString())
                        .get("senhaTemporaria")
                        .asText();
        assertNotEquals(
                corporativo,
                jdbc.queryForObject(
                        "SELECT email FROM funcionario WHERE id=?", String.class, alvo));
        var nova = new MockHttpSession();
        mvc.perform(
                        entrar().session(nova)
                                .header("X-XSRF-TOKEN", csrf(nova))
                                .param("emailCorporativo", corporativo)
                                .param("senha", temporaria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("trocarSenha").value(true));
        mvc.perform(get("/aratech/rh/funcionarios").session(nova))
                .andExpect(status().isForbidden());
        assertThrows(AuthException.class, () -> senhas.criarAcessoCorporativo(alvo, corporativo));
        Long outro =
                jdbc.queryForObject(
                        "INSERT INTO funcionario(nome_completo,email,ativo) VALUES ('Outro"
                                + " teste',?,TRUE) RETURNING id",
                        Long.class,
                        UUID.randomUUID() + "@example.test");
        assertThrows(
                AuthException.class,
                () -> senhas.criarAcessoCorporativo(outro, corporativo.toUpperCase(Locale.ROOT)));
        assertThrows(AuthException.class, () -> senhas.criarAcessoCorporativo(outro, "invalido"));
        assertEquals(
                0L,
                jdbc.queryForObject(
                        "SELECT count(*) FROM acesso_conta WHERE funcionario_id=?",
                        Long.class,
                        outro));
    }

    @Test
    void meuPerfilEPontoSaoDaSessaoSemExigirPermissaoDeRH() throws Exception {
        jdbc.update(
                "UPDATE funcionario SET"
                    + " cargo='Analista',telefone_fixo='1133334444',data_admissao='2025-01-10',tipo_contrato='CLT'"
                    + " WHERE id=?",
                id);
        var session = login();
        mvc.perform(get("/aratech/auth/meu-perfil").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("nome").value("Usuário de segurança"))
                .andExpect(jsonPath("cargo").value("Analista"))
                .andExpect(jsonPath("telefone").value("1133334444"))
                .andExpect(jsonPath("dataAdmissao").value("2025-01-10"))
                .andExpect(jsonPath("salario").doesNotExist());
        mvc.perform(
                        get("/aratech/auth/meu-ponto")
                                .param("mes", "2026-01")
                                .param("id", "999")
                                .session(session))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mvc.perform(get("/aratech/rh/ponto/999").param("mes", "2026-01").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void ajusteDePontoPersisteHistoricoExigePermissaoERecusaConflito() throws Exception {
        Long perfil = permitir("RH.CONSULTAR", "RH.ALTERAR");
        jdbc.update("INSERT INTO funcionario_perfil_acesso(funcionario_id,perfil_id) SELECT ?,id FROM perfil_acesso WHERE nome='#'", id);
        var session = login();
        String data = LocalDate.now().minusDays(1).toString();
        var payload =
                Map.of(
                        "horarios",
                        List.of("08:00", "12:00", "13:00", "17:00", "", ""),
                        "motivo",
                        "Correção de marcação esquecida",
                        "versao",
                        0);
        String uri = "/aratech/rh/ponto/" + id + "/" + data;
        mvc.perform(
                        put(uri).session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("versao").value(1));
        mvc.perform(
                        put(uri).session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isConflict());
        mvc.perform(get(uri + "/historico").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].motivo").value("Correção de marcação esquecida"))
                .andExpect(jsonPath("$[0].anteriores").value(",,,,,"));
        var invalido =
                Map.of(
                        "horarios",
                        List.of("12:00", "08:00", "", "", "", ""),
                        "motivo",
                        "Correção inválida",
                        "versao",
                        1);
        mvc.perform(
                        put(uri).session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
        assertEquals(
                1L,
                jdbc.queryForObject(
                        "SELECT count(*) FROM rh_ponto_historico WHERE funcionario_id=?",
                        Long.class,
                        id));
        jdbc.update(
                "DELETE FROM perfil_permissao_acesso WHERE perfil_id=? AND permissao='RH.ALTERAR'",
                perfil);
        jdbc.update("DELETE FROM funcionario_perfil_acesso WHERE funcionario_id=? AND perfil_id IN (SELECT id FROM perfil_acesso WHERE nome='#')", id);
        mvc.perform(
                        put(uri).session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void marcaPontoSemPermissaoRhExigeCsrfERecusaReenvio() throws Exception {
        var session = login();
        mvc.perform(get("/aratech/auth/meu-ponto/relogio").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("instante").exists());
        String data = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString();
        String payload = mapper.writeValueAsString(Map.of("data", data, "versao", 0));
        mvc.perform(post("/aratech/auth/meu-ponto/marcacoes").session(session)
                .param("id", "999999")
                .contentType("application/json").content(payload)).andExpect(status().isForbidden());
        mvc.perform(post("/aratech/auth/meu-ponto/marcacoes").session(session)
                .with(r -> { r.setRemoteAddr(ipTeste); return r; })
                .header("X-XSRF-TOKEN", csrf(session)).contentType("application/json").content(payload))
                .andExpect(status().isOk()).andExpect(jsonPath("versao").value(1))
                .andExpect(jsonPath("senha").doesNotExist());
        mvc.perform(post("/aratech/auth/meu-ponto/marcacoes").session(session)
                .with(r -> { r.setRemoteAddr(ipTeste); return r; })
                .header("X-XSRF-TOKEN", csrf(session)).contentType("application/json").content(payload))
                .andExpect(status().isConflict());
        assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM rh_ponto_historico WHERE funcionario_id=?", Long.class, id));
    }

    @Test
    void marcacaoNaoAceitaSessaoDeContaDesativada() throws Exception {
        var session = login();
        String token = csrf(session);
        jdbc.update("UPDATE funcionario SET ativo=FALSE WHERE id=?", id);
        String data = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString();
        mvc.perform(post("/aratech/auth/meu-ponto/marcacoes").session(session)
                .header("X-XSRF-TOKEN", token).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("data", data, "versao", 0))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("codigo").value("CSRF_INVALIDO"));
        assertTrue(session.isInvalid(), "A conta inativa deve ter a sessão revogada.");
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM rh_ponto_dia WHERE funcionario_id=?", Long.class, id));
    }

    @Test
    void endpointsDeRecuperacaoForamRemovidos() throws Exception {
        var session = login();
        for (String caminho :
                List.of("/aratech/auth/redefinir-senha", "/aratech/auth/alterar-senha")) {
            mvc.perform(
                            put(caminho)
                                    .session(session)
                                    .header("X-XSRF-TOKEN", csrf(session))
                                    .contentType("application/json")
                                    .content("{}"))
                    .andExpect(status().isForbidden());
        }
        assertNull(jdbc.queryForObject("SELECT to_regclass('acesso_token_senha')", String.class));
    }

    @Test
    void gestorNaoConcedePermissoesSuperioresAsSuas() throws Exception {
        permitir("ACESSOS.GERENCIAR", "RH.CONSULTAR");
        Long perfil =
                jdbc.queryForObject(
                        "INSERT INTO perfil_acesso(nome) VALUES (?) RETURNING id",
                        Long.class,
                        "ALVO-" + UUID.randomUUID());
        var session = login();
        mvc.perform(
                        put("/aratech/auth/usuarios/perfis/" + perfil + "/permissoes")
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content("[\"FINANCEIRO.CONSULTAR\"]"))
                .andExpect(status().isForbidden());
        assertEquals(
                0L,
                jdbc.queryForObject(
                        "SELECT count(*) FROM perfil_permissao_acesso WHERE perfil_id=?",
                        Long.class,
                        perfil));
    }

    @Test
    void senhaTemporariaExigeTrocaAntesDeLiberarPermissoes() throws Exception {
        permitir("RH.CONSULTAR");
        jdbc.update("DELETE FROM acesso_conta WHERE funcionario_id=?", id);
        String temporaria = senhas.criarAcesso(id);
        assertEquals(24, temporaria.length());
        assertThrows(AuthException.class, () -> senhas.criarAcesso(id));
        assertFalse(
                jdbc.queryForObject(
                                "SELECT senha_hash FROM acesso_conta WHERE funcionario_id=?",
                                String.class,
                                id)
                        .contains(temporaria));
        var session = new MockHttpSession();
        mvc.perform(
                        entrar().session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .param("emailCorporativo", email)
                                .param("senha", temporaria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocarSenha").value(true));
        mvc.perform(get("/aratech/rh/funcionarios").session(session))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/aratech/auth/senha")
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content(
                                        mapper.writeValueAsString(
                                                Map.of(
                                                        "senhaAtual",
                                                        temporaria,
                                                        "novaSenha",
                                                        senha))))
                .andExpect(status().isOk());
        mvc.perform(get("/aratech/auth/sessao").session(session))
                .andExpect(status().isUnauthorized());
        var definitiva = login();
        mvc.perform(get("/aratech/auth/sessao").session(definitiva))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocarSenha").value(false));
        mvc.perform(get("/aratech/rh/funcionarios").session(definitiva)).andExpect(status().isOk());
    }

    @Test
    void gestorNaoCriaAcessoParaContaComMaisPermissoes() throws Exception {
        permitir("ACESSOS.GERENCIAR");
        Long alvo =
                jdbc.queryForObject(
                        "INSERT INTO funcionario(nome_completo,email,ativo) VALUES ('Alvo de"
                                + " teste',?,TRUE) RETURNING id",
                        Long.class,
                        UUID.randomUUID() + "@example.test");
        Long perfil =
                jdbc.queryForObject(
                        "INSERT INTO perfil_acesso(nome) VALUES (?) RETURNING id",
                        Long.class,
                        "FIN-" + UUID.randomUUID());
        jdbc.update(
                "INSERT INTO funcionario_perfil_acesso(funcionario_id,perfil_id) VALUES (?,?)",
                alvo,
                perfil);
        jdbc.update(
                "INSERT INTO perfil_permissao_acesso(perfil_id,permissao) VALUES"
                        + " (?,'FINANCEIRO.CONSULTAR')",
                perfil);
        var session = login();
        mvc.perform(
                        post("/aratech/auth/usuarios/" + alvo + "/acesso")
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session)))
                .andExpect(status().isForbidden());
        assertEquals(
                0L,
                jdbc.queryForObject(
                        "SELECT count(*) FROM acesso_conta WHERE funcionario_id=?",
                        Long.class,
                        alvo));
    }

    @Test
    void loginDeFuncionarioInativoEhRejeitadoEExpiracaoAbsolutaRevogaSessao() throws Exception {
        var session = login();
        var contexto =
                (org.springframework.security.core.context.SecurityContext)
                        session.getAttribute("SPRING_SECURITY_CONTEXT");
        var u = (UsuarioSessao) contexto.getAuthentication().getPrincipal();
        var antigo =
                new UsuarioSessao(
                        u.id(),
                        u.nome(),
                        u.email(),
                        u.administrador(),
                        u.versao(),
                        Instant.now().minusSeconds(9 * 3600),
                        u.perfis(),
                        u.permissoes());
        contexto.setAuthentication(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                        .authenticated(antigo, null, SessaoAuthorities.authorities(antigo)));
        mvc.perform(get("/aratech/auth/sessao").session(session))
                .andExpect(status().isUnauthorized());
        jdbc.update("UPDATE funcionario SET ativo=FALSE WHERE id=?", id);
        var nova = new MockHttpSession();
        mvc.perform(
                        entrar().session(nova)
                                .header("X-XSRF-TOKEN", csrf(nova))
                                .param("emailCorporativo", email)
                                .param("senha", senha))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void alterarDadosDoRhNaoPermiteTomarCredencialDeLogin() throws Exception {
        permitir("RH.CONSULTAR", "RH.ALTERAR");
        var session = login();
        mvc.perform(
                        multipart("/aratech/rh/funcionarios/" + id)
                                .with(
                                        request -> {
                                            request.setMethod("PUT");
                                            return request;
                                        })
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .param("email", "outra@example.test"))
                .andExpect(status().isForbidden());
        assertEquals(
                email,
                jdbc.queryForObject("SELECT email FROM funcionario WHERE id=?", String.class, id));
    }

    @Test
    void adminTecnicoSoLiberaModulosDepoisDeTrocarSenhaTemporaria() throws Exception {
        email = "admin-teste-" + UUID.randomUUID() + "@aratechmoveis";
        jdbc.update(
                "INSERT INTO acesso_administrador(id,login,senha_hash,trocar_senha) VALUES"
                        + " (0,?,?,TRUE) ON CONFLICT(id) DO UPDATE SET login=EXCLUDED.login,"
                        + " senha_hash=EXCLUDED.senha_hash, trocar_senha=TRUE, ativo=TRUE",
                email,
                encoder.encode("admi"));
        var session = new MockHttpSession();
        mvc.perform(
                        entrar().session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .param("emailCorporativo", email)
                                .param("senha", "admi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("trocarSenha").value(true));
        mvc.perform(get("/aratech/auth/usuarios/permissoes").session(session))
                .andExpect(status().isForbidden());
        mvc.perform(get("/aratech/rh/funcionarios").session(session))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/aratech/auth/senha")
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content("{\"senhaAtual\":\"admi\",\"novaSenha\":\"admi\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/aratech/auth/senha")
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content(
                                        "{\"senhaAtual\":\"admi\",\"novaSenha\":\"Uma frase nova e"
                                                + " segura!\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/aratech/auth/sessao").session(session))
                .andExpect(status().isUnauthorized());
        var nova = new MockHttpSession();
        mvc.perform(
                        entrar().session(nova)
                                .header("X-XSRF-TOKEN", csrf(nova))
                                .param("emailCorporativo", email)
                                .param("senha", "Uma frase nova e segura!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("trocarSenha").value(false));
        mvc.perform(get("/aratech/auth/usuarios/permissoes").session(nova))
                .andExpect(status().isOk());
    }

    @Test
    void cookieTemFlagsSegurasEApiNaoPodeSerEmbutida() throws Exception {
        var client = HttpClient.newHttpClient();
        var response =
                client.send(
                        HttpRequest.newBuilder(
                                        URI.create(
                                                "http://localhost:" + port + "/aratech/auth/csrf"))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        String cookie = response.headers().firstValue("set-cookie").orElseThrow();
        assertTrue(cookie.contains("ARATECHSESSION="));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("SameSite=Strict"));
        assertTrue(
                response.headers()
                        .firstValue("content-security-policy")
                        .orElseThrow()
                        .contains("frame-ancestors 'none'"));
        assertTrue(
                response.headers().firstValue("cache-control").orElseThrow().contains("no-store"));
    }
}
