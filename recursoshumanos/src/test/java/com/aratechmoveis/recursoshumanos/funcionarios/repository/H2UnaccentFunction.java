package com.aratechmoveis.recursoshumanos.funcionarios.repository;

import java.text.Normalizer;

/**
 * O H2 (usado nos testes) não possui a extensão unaccent do PostgreSQL.
 * Essa classe é registrada como alias no H2 para reproduzir o mesmo comportamento
 * e validar a query nativa usada em produção.
 */
public class H2UnaccentFunction {

    public static String unaccent(String input) {
        if (input == null) return null;
        return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
