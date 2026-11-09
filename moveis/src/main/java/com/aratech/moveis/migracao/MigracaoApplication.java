package com.aratech.moveis.migracao;

import com.aratech.moveis.config.repository.imp.PermissoesMigracaoCallback;
import com.aratech.moveis.config.repository.imp.PreflightMigracao;
import org.flywaydb.core.Flyway;
import java.sql.DriverManager;
import java.util.Set;

/** Entrada operacional do mesmo JAR; não inicia HTTP nem carrega dados demonstrativos. */
public final class MigracaoApplication {
    private MigracaoApplication() {}

    public static void main(String[] argumentos) throws Exception {
        if (argumentos.length != 1 || !Set.of("validar", "migrar", "informar", "adotar").contains(argumentos[0]))
            throw new IllegalArgumentException("Use validar, migrar, informar ou adotar. Clean e repair não são disponibilizados.");
        String url = obrigatoria("ARATECH_MIGRACAO_URL"), usuario = obrigatoria("ARATECH_MIGRACAO_USUARIO"), senha = obrigatoria("ARATECH_MIGRACAO_SENHA");
        String aplicacao = obrigatoria("ARATECH_APLICACAO_USUARIO"), banco = obrigatoria("ARATECH_MIGRACAO_BANCO_ESPERADO");
        String esquema = System.getenv().getOrDefault("ARATECH_MIGRACAO_ESQUEMA", "public");
        try (var conexao = DriverManager.getConnection(url, usuario, senha)) {
            PreflightMigracao.conferirDestino(conexao, banco, esquema);
            PermissoesMigracaoCallback.conferirContaRestrita(conexao, aplicacao);
            if (argumentos[0].equals("adotar")) PreflightMigracao.conferirLegado(conexao, esquema);
        }
        Flyway flyway = PoliticaFlyway.aplicar(Flyway.configure(), esquema)
                .dataSource(url, usuario, senha).callbacks(new PermissoesMigracaoCallback(aplicacao)).load();
        switch (argumentos[0]) {
            case "validar" -> flyway.validate();
            case "informar" -> {
                for (var migracao : flyway.info().all())
                    System.out.println(migracao.getVersion() + " | " + migracao.getDescription() + " | " + migracao.getState());
            }
            case "adotar" -> { flyway.baseline(); flyway.migrate(); flyway.validate(); }
            case "migrar" -> { flyway.migrate(); flyway.validate(); }
            default -> throw new IllegalStateException();
        }
    }

    private static String obrigatoria(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Configure " + nome + ".");
        return valor;
    }
}
