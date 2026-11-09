package com.aratech.moveis.security.validation;

import com.aratech.moveis.security.exception.AuthException;

import java.util.Locale;
import java.util.regex.Pattern;

/** Identificador interno; não pressupõe a existência de uma caixa postal. */
public final class LoginCorporativo {
    public static final String REGEX =
            "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@aratechmoveis$";
    private static final Pattern PADRAO = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private LoginCorporativo() {}

    public static String normalizar(String valor) {
        return valor == null ? "" : valor.strip().toLowerCase(Locale.ROOT);
    }

    public static boolean valido(String valor) {
        String login = normalizar(valor);
        int arroba = login.indexOf('@');
        return login.length() <= 150
                && arroba > 0
                && arroba <= 64
                && PADRAO.matcher(login).matches();
    }

    public static String exigir(String valor) {
        if (!valido(valor))
            throw new AuthException(
                    400,
                    "Informe um login válido no formato nome@aratechmoveis, com até 64 caracteres"
                            + " antes do @.");
        return normalizar(valor);
    }
}
