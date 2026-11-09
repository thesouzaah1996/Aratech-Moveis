package com.aratech.moveis.security;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.security.validation.LoginCorporativo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LoginCorporativoTest {
    @ParameterizedTest
    @ValueSource(
            strings = {
                "admin@aratechmoveis",
                "ana.silva@aratechmoveis",
                " ANA.SILVA@ARATECHMOVEIS ",
                "ana+pcp@aratechmoveis"
            })
    void aceitaDominioInterno(String login) {
        assertTrue(LoginCorporativo.valido(login));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                "admin",
                "admin@example.com",
                "admin@aratechmoveis.com",
                "admin@sub.aratechmoveis",
                "@aratechmoveis",
                ".ana@aratechmoveis",
                "ana.@aratechmoveis",
                "ana..silva@aratechmoveis",
                "ana silva@aratechmoveis",
                "ana@@aratechmoveis",
                "ana@aratechmoveis.evil",
                "ana@aratechmoveis\nmalicioso"
            })
    void rejeitaFormatoOuDominioInvalido(String login) {
        assertFalse(LoginCorporativo.valido(login));
        assertEquals(
                400,
                assertThrows(AuthException.class, () -> LoginCorporativo.exigir(login)).status());
    }

    @Test
    void limitaParteLocalENormaliza() {
        assertFalse(LoginCorporativo.valido(null));
        assertTrue(LoginCorporativo.valido("a".repeat(64) + "@aratechmoveis"));
        assertFalse(LoginCorporativo.valido("a".repeat(65) + "@aratechmoveis"));
        assertEquals("ana@aratechmoveis", LoginCorporativo.exigir(" ANA@ARATECHMOVEIS "));
    }
}
