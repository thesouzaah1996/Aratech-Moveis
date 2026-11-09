package com.aratech.moveis.config.repository.imp;

import org.flywaydb.core.api.callback.*;
import java.sql.*;

/** Executado somente pela tarefa Flyway, com a conta proprietária do schema. */
public final class PermissoesMigracaoCallback implements Callback {
    private final String aplicacao;

    public PermissoesMigracaoCallback(String aplicacao) {
        this.aplicacao = identificador(aplicacao);
    }

    public static String identificador(String nome) {
        if (nome == null || !nome.matches("[a-z_][a-z0-9_]{0,62}"))
            throw new IllegalArgumentException("Identificador PostgreSQL inválido.");
        return nome;
    }

    @Override public boolean supports(Event evento, Context contexto) { return evento == Event.AFTER_MIGRATE || evento == Event.BEFORE_EACH_MIGRATE; }
    @Override public boolean canHandleInTransaction(Event evento, Context contexto) { return true; }
    @Override public String getCallbackName() { return "permissoes_minimas_da_aplicacao"; }

    @Override
    public void handle(Event evento, Context contexto) {
        if (evento == Event.BEFORE_EACH_MIGRATE) {
            com.aratech.moveis.config.repository.imp.CatalogoPermissoesMigracaoCallback.registrar(contexto);
            return;
        }
        try {
            Connection conexao = contexto.getConnection();
            conferirContaRestrita(conexao, aplicacao);
            conferirContaRestrita(conexao, conexao.getMetaData().getUserName());
            String esquema = identificador(contexto.getConfiguration().getDefaultSchema());
            try (var consulta = conexao.prepareStatement("SELECT pg_has_role(?, current_user, 'MEMBER')")) {
                consulta.setString(1, aplicacao);
                try (var resultado = consulta.executeQuery()) {
                    resultado.next();
                    if (resultado.getBoolean(1)) throw new SQLException("A aplicação não pode assumir a conta de migração.");
                }
            }
            String schemaSql = '"' + esquema + '"', usuarioSql = '"' + aplicacao + '"';
            try (var sql = conexao.createStatement()) {
                sql.execute("REVOKE CREATE ON SCHEMA " + schemaSql + " FROM PUBLIC, " + usuarioSql);
                sql.execute("GRANT USAGE ON SCHEMA " + schemaSql + " TO " + usuarioSql);
                sql.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA " + schemaSql + " TO " + usuarioSql);
                sql.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA " + schemaSql + " TO " + usuarioSql);
                sql.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA " + schemaSql + " GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO " + usuarioSql);
                sql.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA " + schemaSql + " GRANT USAGE, SELECT ON SEQUENCES TO " + usuarioSql);
                String historico = schemaSql + ".flyway_schema_history";
                sql.execute("REVOKE ALL ON TABLE " + historico + " FROM PUBLIC, " + usuarioSql);
                // Leitura permite validar versão/checksum na inicialização, sem poder alterar o histórico.
                sql.execute("GRANT SELECT ON TABLE " + historico + " TO " + usuarioSql);
            }
        } catch (SQLException erro) {
            throw new IllegalStateException("Não foi possível aplicar as permissões mínimas do banco.", erro);
        }
    }

    public static void conferirContaRestrita(Connection conexao, String usuario) throws SQLException {
        try (var consulta = conexao.prepareStatement("SELECT rolsuper, rolcreatedb, rolcreaterole, rolreplication, rolbypassrls FROM pg_roles WHERE rolname=?")) {
            consulta.setString(1, usuario);
            try (var resultado = consulta.executeQuery()) {
                if (!resultado.next()) throw new SQLException("Conta de banco não encontrada.");
                for (int coluna = 1; coluna <= 5; coluna++)
                    if (resultado.getBoolean(coluna)) throw new SQLException("A aplicação e o migrador não podem usar contas administrativas.");
            }
        }
    }
}
