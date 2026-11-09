package com.aratech.moveis.config;

import com.aratech.moveis.migracao.PoliticaFlyway;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {
    @Bean
    FlywayConfigurationCustomizer protecoesFlyway() {
        return configuracao -> PoliticaFlyway.aplicar(configuracao, configuracao.getDefaultSchema());
    }

    /** O processo HTTP nunca executa DDL. A atualização pertence à tarefa de migração. */
    @Bean
    FlywayMigrationStrategy validarBancoAntesDeIniciar() {
        return org.flywaydb.core.Flyway::validate;
    }
}
