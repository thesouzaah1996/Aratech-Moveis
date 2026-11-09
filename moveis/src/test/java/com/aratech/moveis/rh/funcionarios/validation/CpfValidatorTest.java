package com.aratech.moveis.rh.funcionarios.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfValidatorTest {
    private final CpfValidator validator = new CpfValidator();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "1",
                "5299822472",
                "529982247250",
                "529.982.247-25",
                "5299822472a",
                "11111111111",
                "00000000000",
                "52998224724"
            })
    void rejeitaCpfInvalidoSemLancarExcecao(String cpf) {
        assertFalse(validator.isValid(cpf));
    }

    @Test
    void aceitaCpfComDigitosVerificadoresCorretos() {
        assertTrue(validator.isValid("52998224725"));
    }
}
