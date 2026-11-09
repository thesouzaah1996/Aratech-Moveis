package com.aratech.moveis.manutencao.pecas;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaDTO;
import com.aratech.moveis.manutencao.pecas.validation.SolicitacaoPecaValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.math.BigDecimal;
import java.util.UUID;

class SolicitacaoPecaValidatorTest {
    final SolicitacaoPecaValidator v = new SolicitacaoPecaValidator(new ChamadoValidator());

    SolicitacaoPecaDTO dto(BigDecimal quantidade, String unidade, String finalidade) {
        return new SolicitacaoPecaDTO(
                " Rolamento  6205 ",
                " SKF ",
                quantidade,
                unidade,
                "Prensa 01",
                finalidade,
                "ALTA",
                "Ana Silva",
                "Producao",
                "(43) 3333-1234",
                "Falha no motor",
                UUID.randomUUID());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.001", "1", "1.500", "999999999999.999"})
    void aceitaQuantidadesValidas(String q) {
        assertEquals(
                new BigDecimal(q),
                v.validar(dto(new BigDecimal(q), "kg", "CORRETIVA")).quantidade());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.0001", "1000000000000"})
    void rejeitaQuantidadeInvalida(String q) {
        assertThrows(
                ChamadoException.class, () -> v.validar(dto(new BigDecimal(q), "un", "CORRETIVA")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"un", "kg", "L", "m", "cx", "par"})
    void aceitaUnidades(String un) {
        assertEquals(un, v.validar(dto(BigDecimal.ONE, un, "CORRETIVA")).unidade());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CORRETIVA", "PREVENTIVA", "MELHORIA", "ESTOQUE"})
    void aceitaFinalidades(String fin) {
        assertEquals(fin, v.validar(dto(BigDecimal.ONE, "un", fin)).finalidade());
    }

    @Test
    void normalizaCamposERejeitaDadosAusentes() {
        var d = v.validar(dto(BigDecimal.ONE, "un", "CORRETIVA"));
        assertEquals("Rolamento 6205", d.nomePeca());
        assertEquals("SKF", d.codigo());
        assertEquals("4333331234", d.telefone());
        assertThrows(ChamadoException.class, () -> v.validar(null));
        assertThrows(ChamadoException.class, () -> v.validar(dto(null, "un", "CORRETIVA")));
        assertThrows(
                ChamadoException.class, () -> v.validar(dto(BigDecimal.ONE, "outro", "CORRETIVA")));
        assertThrows(ChamadoException.class, () -> v.validar(dto(BigDecimal.ONE, "un", "OUTRA")));
    }
}
