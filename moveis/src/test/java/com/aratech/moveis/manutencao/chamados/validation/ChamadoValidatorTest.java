package com.aratech.moveis.manutencao.chamados.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.manutencao.chamados.dto.ChamadoDTO;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.UUID;

class ChamadoValidatorTest {
    final ChamadoValidator validator = new ChamadoValidator();

    ChamadoDTO dto(String tipo, String prioridade, String telefone) {
        return new ChamadoDTO(
                "  Prensa   01  ",
                tipo,
                prioridade,
                "  Ana   Silva  ",
                " Producao ",
                telefone,
                "  Falha na prensa\nVerificar motor.  ",
                UUID.randomUUID());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CORRETIVA", "PREVENTIVA"})
    void normalizaTextosEPreservaDescricaoMultilinha(String tipo) {
        var d = validator.validar(dto(tipo, "ALTA", "(43) 99999-1234"));
        assertEquals("Prensa 01", d.equipamento());
        assertEquals("Ana Silva", d.solicitante());
        assertEquals("Producao", d.setor());
        assertEquals("43999991234", d.telefone());
        assertEquals("Falha na prensa\nVerificar motor.", d.descricao());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BAIXA", "MEDIA", "ALTA"})
    void aceitaPrioridades(String prioridade) {
        assertEquals(prioridade, validator.validar(dto("CORRETIVA", prioridade, "")).prioridade());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"(43) 3333-1234", "43999991234"})
    void telefoneOpcionalFixoOuCelular(String telefone) {
        assertNotNull(validator.validar(dto("CORRETIVA", "BAIXA", telefone)));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"123", "(00) 12345-1234", "123456789012", "abc43999991234", "43\n999991234"})
    void rejeitaTelefoneInvalido(String telefone) {
        assertThrows(
                ChamadoException.class,
                () -> validator.validar(dto("CORRETIVA", "ALTA", telefone)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"OUTRO", "corretiva", " CORRETIVA "})
    void rejeitaTipoInvalido(String tipo) {
        assertThrows(ChamadoException.class, () -> validator.validar(dto(tipo, "ALTA", "")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"URGENTE", "alta"})
    void rejeitaPrioridadeInvalida(String prioridade) {
        assertThrows(
                ChamadoException.class, () -> validator.validar(dto("CORRETIVA", prioridade, "")));
    }

    @Test
    void exigeDadosObrigatoriosELimites() {
        assertThrows(ChamadoException.class, () -> validator.validar(null));
        for (String equipamento :
                new String[] {null, " ", "A", "A".repeat(151), "Prensa\n01", "Prensa\u200B01"})
            assertThrows(
                    ChamadoException.class,
                    () ->
                            validator.validar(
                                    new ChamadoDTO(
                                            equipamento,
                                            "CORRETIVA",
                                            "ALTA",
                                            "Ana Silva",
                                            "Producao",
                                            "",
                                            "Motor parado",
                                            UUID.randomUUID())));
        assertThrows(
                ChamadoException.class,
                () ->
                        validator.validar(
                                new ChamadoDTO(
                                        "Prensa",
                                        "CORRETIVA",
                                        "ALTA",
                                        "A",
                                        "Producao",
                                        "",
                                        "Motor parado",
                                        UUID.randomUUID())));
        assertThrows(
                ChamadoException.class,
                () ->
                        validator.validar(
                                new ChamadoDTO(
                                        "Prensa",
                                        "CORRETIVA",
                                        "ALTA",
                                        "Ana Silva",
                                        "P",
                                        "",
                                        "Motor parado",
                                        UUID.randomUUID())));
        assertThrows(
                ChamadoException.class,
                () ->
                        validator.validar(
                                new ChamadoDTO(
                                        "Prensa",
                                        "CORRETIVA",
                                        "ALTA",
                                        "Ana Silva",
                                        "Producao",
                                        "",
                                        "A".repeat(3001),
                                        UUID.randomUUID())));
        assertThrows(
                ChamadoException.class,
                () ->
                        validator.validar(
                                new ChamadoDTO(
                                        "Prensa",
                                        "CORRETIVA",
                                        "ALTA",
                                        "Ana Silva",
                                        "Producao",
                                        "",
                                        "Motor\u0000parado",
                                        UUID.randomUUID())));
        assertThrows(
                ChamadoException.class,
                () ->
                        validator.validar(
                                new ChamadoDTO(
                                        "Prensa",
                                        "CORRETIVA",
                                        "ALTA",
                                        "Ana Silva",
                                        "Producao",
                                        "",
                                        "Motor parado",
                                        null)));
    }

    @Test
    void validaIdPaginacaoEStatus() {
        assertThrows(ChamadoException.class, () -> validator.id(0L));
        assertThrows(ChamadoException.class, () -> validator.id(null));
        assertThrows(ChamadoException.class, () -> validator.paginacao(-1, 8));
        assertThrows(ChamadoException.class, () -> validator.paginacao(0, 101));
        assertThrows(ChamadoException.class, () -> validator.paginacao(Integer.MAX_VALUE, 100));
        assertThrows(ChamadoException.class, () -> validator.status("OUTRO"));
    }
}
