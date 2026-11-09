package com.aratech.moveis.migracao;
import com.aratech.moveis.config.repository.imp.CatalogoPermissoesMigracaoCallback;

import org.flywaydb.core.api.configuration.FluentConfiguration;

public final class PoliticaFlyway {
    private PoliticaFlyway() {}

    public static FluentConfiguration aplicar(FluentConfiguration configuracao, String esquema) {
        if (esquema == null || esquema.isBlank()) esquema = "public";
        if (!esquema.matches("[a-z_][a-z0-9_]{0,62}"))
            throw new IllegalArgumentException("Nome de schema inválido.");
        return configuracao.callbacks(new CatalogoPermissoesMigracaoCallback()).locations("classpath:db/migration")
                .schemas(esquema).defaultSchema(esquema).createSchemas(false)
                .baselineOnMigrate(false).baselineVersion("0")
                .validateOnMigrate(true).validateMigrationNaming(true)
                .cleanDisabled(true).outOfOrder(false).encoding("UTF-8")
                .ignoreMigrationPatterns(new String[0]);
    }
}
