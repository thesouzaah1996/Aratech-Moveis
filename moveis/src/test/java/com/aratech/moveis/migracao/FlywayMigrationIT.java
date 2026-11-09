package com.aratech.moveis.migracao;

import static org.junit.jupiter.api.Assertions.*;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.*;

import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Usa schemas exclusivos no PostgreSQL dev, sem migrar o schema da aplicação. */
class FlywayMigrationIT {
    private Connection connection;
    private String url;
    private String usuario;
    private String senha;
    private String schema;

    @BeforeEach
    void preparar() throws Exception {
        var propriedades = new Properties();
        Path env = Path.of(".env");
        if (Files.exists(env)) {
            try (var reader = Files.newBufferedReader(env)) { propriedades.load(reader); }
        }
        usuario = valor(propriedades, "POSTGRES_USER_DEV", "");
        senha = valor(propriedades, "POSTGRES_PASSWORD_DEV", "");
        url = "jdbc:postgresql://" + valor(propriedades, "DB_HOST_DEV", "localhost") + ":"
                + valor(propriedades, "DB_PORT_DEV", "5435") + "/"
                + valor(propriedades, "POSTGRES_DB_DEV", "aratechmoveis_dev");
        connection = DriverManager.getConnection(url, usuario, senha);
        schema = "flyway_it_" + UUID.randomUUID().toString().replace("-", "");
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
            statement.execute("SET search_path TO " + schema);
        }
    }

    private String valor(Properties propriedades, String nome, String padrao) {
        return System.getProperty(nome, System.getenv().getOrDefault(nome, propriedades.getProperty(nome, padrao)));
    }

    private Flyway flyway(boolean adotar) {
        var flyway = PoliticaFlyway.aplicar(Flyway.configure(), schema)
                .dataSource(url, usuario, senha).load();
        if (adotar) flyway.baseline();
        return flyway;
    }

    @AfterEach
    void limpar() throws Exception {
        if (connection == null) return;
        try (var conexao = connection; var statement = conexao.createStatement()) {
            if (schema != null && schema.matches("flyway_it_[a-f0-9]{32}"))
                statement.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }

    @Test
    void instalaBancoVazioEReiniciaSemReaplicar() throws Exception {
        var flyway = flyway(false);
        assertEquals(17, flyway.migrate().migrationsExecuted);
        assertEquals("17", flyway.info().current().getVersion().toString());
        try (var statement = connection.createStatement()) {
            for (String tabela : List.of("funcionario", "produto", "acesso_conta", "registro_chegada",
                    "pcp_ordem", "financeiro_titulo", "assistencia_lote", "suporte_ti_chamado")) {
                try (var result = statement.executeQuery("SELECT to_regclass('" + schema + "." + tabela + "')")) {
                    assertTrue(result.next());
                    assertNotNull(result.getString(1), tabela);
                }
            }
        }
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertTrue(flyway.validateWithResult().validationSuccessful);
        assertThrows(FlywayException.class, flyway::clean);
    }

    @Test
    void adotaBancoLegadoEPreservaDados() throws Exception {
        // Reproduz os scripts antes executados pelos ApplicationRunner, sem histórico Flyway.
        try (var statement = connection.createStatement()) {
            for (String script : List.of("V1__almoxarifado.sql", "V2__cadastro_funcionario.sql",
                    "V3__perfis.sql", "V4__seguranca.sql", "V5__ponto.sql",
                    "V6__manutencao_chamados.sql", "V7__manutencao_estoque.sql",
                    "V8__controle_acesso.sql", "V9__compras.sql", "V10__operacional.sql",
                    "V11__assistencia_estoque.sql", "V12__assistencia_lotes.sql", "V13__suporte_ti.sql")) {
                try (var stream = getClass().getResourceAsStream("/db/migration/" + script)) {
                    assertNotNull(stream);
                    statement.execute(new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            statement.execute("INSERT INTO funcionario(nome_completo,email,ativo) VALUES ('Preservar cadastro','preservar@aratechmoveis',TRUE)");
        }
        var flyway = flyway(true);
        assertEquals(17, flyway.migrate().migrationsExecuted);
        try (var statement = connection.createStatement();
                var result = statement.executeQuery("SELECT nome_completo FROM funcionario WHERE email='preservar@aratechmoveis'")) {
            assertTrue(result.next());
            assertEquals("Preservar cadastro", result.getString(1));
        }
        assertEquals(0, flyway.migrate().migrationsExecuted);
    }

    @Test
    void exigeAdocaoExplicitaEDetectaAlteracaoDeMigracaoAplicada() throws Exception {
        try (var statement = connection.createStatement()) { statement.execute("CREATE TABLE legado (id BIGINT)"); }
        assertThrows(FlywayException.class, () -> flyway(false).migrate());
        var flyway = flyway(true);
        flyway.migrate();
        try (var statement = connection.createStatement()) {
            statement.execute("UPDATE flyway_schema_history SET checksum=0 WHERE version='1'");
        }
        assertFalse(flyway.validateWithResult().validationSuccessful);
        assertThrows(FlywayException.class, flyway::migrate);
    }

    @Test
    void preservaConsultaDosLotesMovidosSemConcederEscrita() throws Exception {
        PoliticaFlyway.aplicar(Flyway.configure(),schema).target("14").dataSource(url,usuario,senha).load().migrate();
        long perfil;
        try(var sql=connection.createStatement()) {
            try(var r=sql.executeQuery("INSERT INTO perfil_acesso(nome) VALUES ('Gestor de lotes') RETURNING id")){r.next();perfil=r.getLong(1);}
            sql.execute("INSERT INTO perfil_permissao_acesso(perfil_id,permissao) VALUES ("+perfil+",'ADMINISTRACAO_LOTES.CONSULTAR')");
            sql.execute("DELETE FROM acesso_permissao WHERE codigo='PCP_LOTES.CONSULTAR'");
        }
        assertEquals(3,flyway(false).migrate().migrationsExecuted);
        try(var sql=connection.createStatement();var r=sql.executeQuery("SELECT permissao FROM perfil_permissao_acesso WHERE perfil_id="+perfil+" AND permissao LIKE 'PCP_LOTES.%'")) {
            assertTrue(r.next());assertEquals("PCP_LOTES.CONSULTAR",r.getString(1));assertFalse(r.next());
        }
    }
}
