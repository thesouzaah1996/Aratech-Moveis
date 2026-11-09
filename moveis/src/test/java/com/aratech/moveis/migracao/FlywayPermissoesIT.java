package com.aratech.moveis.migracao;

import static org.junit.jupiter.api.Assertions.*;
import com.aratech.moveis.config.repository.imp.PermissoesMigracaoCallback;
import com.aratech.moveis.config.repository.imp.PreflightMigracao;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

class FlywayPermissoesIT {
    Connection admin;
    String url, schema, app, migrador, senha;
    @TempDir Path temporario;

    @BeforeEach
    void preparar() throws Exception {
        var propriedades = new Properties();
        if (Files.exists(Path.of(".env"))) try (var leitor = Files.newBufferedReader(Path.of(".env"))) { propriedades.load(leitor); }
        url = "jdbc:postgresql://" + valor(propriedades,"DB_HOST_DEV","localhost") + ":" + valor(propriedades,"DB_PORT_DEV","5435") + "/" + valor(propriedades,"POSTGRES_DB_DEV","aratechmoveis_dev");
        admin = DriverManager.getConnection(url,valor(propriedades,"POSTGRES_USER_DEV",""),valor(propriedades,"POSTGRES_PASSWORD_DEV",""));
        String chave = UUID.randomUUID().toString().replace("-", "");
        schema = "flyway_priv_" + chave; app = "fw_app_" + chave; migrador = "fw_mig_" + chave; senha = UUID.randomUUID().toString();
        try (var sql = admin.createStatement()) {
            for (String role : List.of(app,migrador))
                sql.execute("CREATE ROLE " + role + " LOGIN PASSWORD '" + senha + "' NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS NOINHERIT");
            sql.execute("CREATE SCHEMA " + schema + " AUTHORIZATION " + migrador);
        }
    }

    private String valor(Properties p, String nome, String padrao) {
        return System.getProperty(nome,System.getenv().getOrDefault(nome,p.getProperty(nome,padrao)));
    }
    private Flyway migracao(String... locais) {
        return PoliticaFlyway.aplicar(Flyway.configure(),schema).dataSource(url,migrador,senha)
                .locations(locais.length == 0 ? new String[]{"classpath:db/migration"} : locais)
                .callbacks(new PermissoesMigracaoCallback(app)).load();
    }
    private Connection aplicacao() throws SQLException {
        var conexao = DriverManager.getConnection(url,app,senha); conexao.setSchema(schema); return conexao;
    }
    private Flyway validadorDaAplicacao() {
        return PoliticaFlyway.aplicar(Flyway.configure(),schema).dataSource(url,app,senha).load();
    }

    @AfterEach
    void limpar() throws Exception {
        if (admin == null) return;
        try (var conexao = admin; var sql = conexao.createStatement()) {
            if (schema != null && schema.matches("flyway_priv_[a-f0-9]{32}")) {
                sql.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
                sql.execute("DROP ROLE IF EXISTS " + app);
                sql.execute("DROP ROLE IF EXISTS " + migrador);
            }
        }
    }

    @Test
    void aplicacaoOperaDadosEValidaSemPoderAlterarEstruturaOuHistorico() throws Exception {
        assertEquals(17,migracao().migrate().migrationsExecuted);
        assertDoesNotThrow(() -> validadorDaAplicacao().validate());
        try (var conexao = aplicacao(); var sql = conexao.createStatement()) {
            sql.executeUpdate("INSERT INTO funcionario(nome_completo,email,ativo) VALUES ('Conta restrita','restrita@aratechmoveis',TRUE)");
            assertEquals(1,sql.executeUpdate("UPDATE funcionario SET nome_completo='Conta conferida' WHERE email='restrita@aratechmoveis'"));
            try (var resultado = sql.executeQuery("SELECT count(*) FROM flyway_schema_history")) { assertTrue(resultado.next()); assertEquals(17,resultado.getInt(1)); }
            assertThrows(SQLException.class,() -> sql.execute("ALTER TABLE funcionario ADD COLUMN indevida TEXT"));
            assertThrows(SQLException.class,() -> sql.execute("DROP TABLE funcionario CASCADE"));
            assertThrows(SQLException.class,() -> sql.execute("CREATE TABLE indevida(id BIGINT)"));
            assertThrows(SQLException.class,() -> sql.execute("CREATE SCHEMA indevido"));
            assertThrows(SQLException.class,() -> sql.execute("UPDATE flyway_schema_history SET checksum=0"));
            assertThrows(SQLException.class,() -> sql.execute("DELETE FROM flyway_schema_history"));
            assertThrows(SQLException.class,() -> sql.execute("TRUNCATE funcionario"));
            assertThrows(SQLException.class,() -> sql.execute("SET ROLE " + migrador));
            assertEquals(1,sql.executeUpdate("DELETE FROM funcionario WHERE email='restrita@aratechmoveis'"));
        }
    }

    @Test
    void rejeitaArquivoMalNomeadoEAtualizaPermissoesDeNovasTabelas() throws Exception {
        migracao().migrate();
        Path invalido = temporario.resolve("V18_nome_invalido.sql");
        Files.writeString(invalido,"SELECT 1;");
        assertThrows(FlywayException.class,() -> migracao("classpath:db/migration","filesystem:" + temporario).migrate());
        Files.delete(invalido);
        Files.writeString(temporario.resolve("V18__recurso_novo.sql"),"CREATE TABLE recurso_novo(id BIGSERIAL PRIMARY KEY, descricao TEXT NOT NULL);");
        var flyway = migracao("classpath:db/migration","filesystem:" + temporario);
        assertEquals(1,flyway.migrate().migrationsExecuted);
        assertEquals(0,flyway.migrate().migrationsExecuted);
        try (var conexao = aplicacao(); var sql = conexao.createStatement()) {
            assertEquals(1,sql.executeUpdate("INSERT INTO recurso_novo(descricao) VALUES ('Teste')"));
            assertThrows(SQLException.class,() -> sql.execute("UPDATE flyway_schema_history SET checksum=0"));
        }
        // Uma aplicação antiga deve recusar um banco com versão desconhecida, sem ignorar future.
        assertThrows(FlywayException.class,() -> validadorDaAplicacao().validate());
    }

    @Test
    void falhaDeMigracaoReverteSeusDadosETabelasSemDesfazerVersoesAnteriores() throws Exception {
        migracao().migrate();
        Files.writeString(temporario.resolve("V18__falha_transacional.sql"),
                "INSERT INTO funcionario(nome_completo,email) VALUES ('Reverter','reverter@aratechmoveis'); CREATE TABLE reverter_tabela(id BIGINT); SELECT 1/0;");
        assertThrows(FlywayException.class,() -> migracao("classpath:db/migration","filesystem:" + temporario).migrate());
        try (var conexao = aplicacao(); var sql = conexao.createStatement()) {
            try (var resultado = sql.executeQuery("SELECT count(*) FROM funcionario WHERE email='reverter@aratechmoveis'")) { resultado.next(); assertEquals(0,resultado.getInt(1)); }
            try (var resultado = sql.executeQuery("SELECT to_regclass('" + schema + ".reverter_tabela')")) { resultado.next(); assertNull(resultado.getString(1)); }
        }
        assertDoesNotThrow(() -> validadorDaAplicacao().validate());
    }

    @Test
    void exigeDestinoRevisadoELegadoReconhecidoAntesDeAdotar() throws Exception {
        try (var conexao = DriverManager.getConnection(url,migrador,senha); var sql = conexao.createStatement()) {
            assertThrows(IllegalArgumentException.class,() -> PreflightMigracao.conferirDestino(conexao,"outro_banco",schema));
            assertDoesNotThrow(() -> PreflightMigracao.conferirDestino(conexao,conexao.getCatalog(),schema));
            assertThrows(IllegalStateException.class,() -> PreflightMigracao.conferirLegado(conexao,schema));
            for (String tabela : List.of("funcionario","categoria","fornecedor","produto","perfil_acesso","acesso_conta","registro_chegada","acesso_token_senha"))
                sql.execute("CREATE TABLE " + schema + "." + tabela + "(id BIGINT)");
            assertThrows(IllegalStateException.class,() -> PreflightMigracao.conferirLegado(conexao,schema));
        }
    }
}
