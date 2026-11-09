package com.aratech.moveis.config.repository.imp;

import java.sql.*;
import java.util.List;

public final class PreflightMigracao {
    private PreflightMigracao() {}

    public static void conferirDestino(Connection conexao, String bancoEsperado, String esquema) throws SQLException {
        if (bancoEsperado == null || bancoEsperado.isBlank() || !bancoEsperado.equals(conexao.getCatalog()))
            throw new IllegalArgumentException("O banco conectado não corresponde ao banco esperado.");
        PermissoesMigracaoCallback.identificador(esquema);
        PermissoesMigracaoCallback.conferirContaRestrita(conexao, conexao.getMetaData().getUserName());
        try (var consulta = conexao.prepareStatement("SELECT nspowner=(SELECT oid FROM pg_roles WHERE rolname=current_user) FROM pg_namespace WHERE nspname=?")) {
            consulta.setString(1, esquema);
            try (var resultado = consulta.executeQuery()) {
                if (!resultado.next() || !resultado.getBoolean(1))
                    throw new IllegalStateException("A conta de migração deve ser proprietária do schema existente.");
            }
        }
    }

    public static void conferirLegado(Connection conexao, String esquema) throws SQLException {
        if (existe(conexao, esquema, "flyway_schema_history"))
            throw new IllegalStateException("Banco já gerenciado pelo Flyway: use validar ou migrar.");
        for (String tabela : List.of("funcionario", "categoria", "fornecedor", "produto", "perfil_acesso", "acesso_conta", "registro_chegada"))
            if (!existe(conexao, esquema, tabela))
                throw new IllegalStateException("Estrutura legada não reconhecida. Faça a conferência antes do baseline.");
        if (existe(conexao, esquema, "acesso_token_senha"))
            throw new IllegalStateException("Existe tabela obsoleta afetada pela V4. Sua remoção exige revisão específica antes da adoção.");
    }

    private static boolean existe(Connection conexao, String esquema, String tabela) throws SQLException {
        try (var consulta = conexao.prepareStatement("SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=? AND table_name=?)")) {
            consulta.setString(1, esquema); consulta.setString(2, tabela);
            try (var resultado = consulta.executeQuery()) { resultado.next(); return resultado.getBoolean(1); }
        }
    }
}
