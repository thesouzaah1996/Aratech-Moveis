package com.aratech.moveis.security;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.security.validation.SenhaValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

class PoliticaSenhaTest {
    @ParameterizedTest
    @ValueSource(
            strings = {
                "k9!Z7@wR",
                "m7#Q2!vR",
                "Uma frase nova e segura!",
                "Frase de teste segura 2026!",
                "Outra frase de senha segura!"
            })
    void aceitaSenhasFortesInclusiveComOitoCaracteres(String senha) {
        assertDoesNotThrow(() -> SenhaValidator.validarSenha(senha));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "curta",
                "12345678",
                "Senha123!",
                "Aratech@2026",
                "Admin@123",
                "Brasil@2026",
                "Qwerty123!",
                "aaaaaaaaaaaaaaaaaaaaaaaa",
                "passwordpassword",
                "1234567890123456",
                "        "
            })
    void rejeitaSenhasComunsCurtasOuPrevisiveisMesmoSeForemLongas(String senha) {
        assertThrows(AuthException.class, () -> SenhaValidator.validarSenha(senha));
    }

    @Test
    void rejeitaTamanhoExcessivoAntesDaAvaliacaoDeForca() {
        assertThrows(AuthException.class, () -> SenhaValidator.validarSenha("X".repeat(129)));
    }
}
