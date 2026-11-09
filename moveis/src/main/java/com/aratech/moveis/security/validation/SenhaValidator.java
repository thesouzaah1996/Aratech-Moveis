package com.aratech.moveis.security.validation;

import com.aratech.moveis.security.exception.AuthException;

import java.util.*;

public final class SenhaValidator {
    private SenhaValidator() {}

    private static final com.nulabinc.zxcvbn.Zxcvbn AVALIADOR = new com.nulabinc.zxcvbn.Zxcvbn();
    private static final List<String> TERMOS_PREVISIVEIS =
            List.of(
                    "aratech",
                    "aratechmoveis",
                    "admin",
                    "administrador",
                    "senha",
                    "moveis",
                    "móveis",
                    "password",
                    "bemvindo",
                    "brasil");

    public static void validarSenha(String senha) {
        if (senha == null
                || senha.codePointCount(0, senha.length()) < 8
                || senha.length() > 128
                || senha.isBlank())
            throw new AuthException(400, "Use uma senha de 8 a 128 caracteres.");
        // A classificação arredondada do zxcvbn trata 100 milhões + 1 como nível 2;
        // usamos a estimativa diretamente para aceitar senhas fortes de oito caracteres.
        double tentativas = AVALIADOR.measure(senha, TERMOS_PREVISIVEIS).getGuesses();
        if (!Double.isFinite(tentativas) || tentativas < 100_000_000d)
            throw new AuthException(
                    400,
                    "Senha muito previsível. Evite palavras comuns, sequências e repetições. Use"
                            + " uma combinação menos previsível ou uma frase maior.");
    }
}
