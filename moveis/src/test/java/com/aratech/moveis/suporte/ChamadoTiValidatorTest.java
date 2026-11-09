package com.aratech.moveis.suporte;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.suporte.chamados.dto.ChamadoTiDTO;
import com.aratech.moveis.suporte.chamados.exception.ChamadoTiException;
import com.aratech.moveis.suporte.chamados.validation.ChamadoTiValidator;

import org.junit.jupiter.api.Test;

import java.util.UUID;

class ChamadoTiValidatorTest {
    final ChamadoTiValidator v = new ChamadoTiValidator();

    ChamadoTiDTO dados(String contato) {
        return new ChamadoTiDTO(
                "Hardware",
                "Alta",
                null,
                "  Monitor  parado  ",
                "Ana Silva",
                "TI",
                contato,
                "Sem imagem\nCabo testado",
                null,
                UUID.randomUUID());
    }

    @Test
    void normalizaCamposEOpcionais() {
        var d = v.validar(dados(null));
        assertEquals("Monitor parado", d.titulo());
        assertEquals("", d.moduloAfetado());
        assertEquals("", d.contato());
        assertEquals("", d.tentativasSolucao());
        assertTrue(d.descricao().contains("\n"));
    }

    @Test
    void aceitaRamalTelefoneECelular() {
        assertEquals("1234", v.validar(dados("1234")).contato());
        assertEquals("1133334444", v.validar(dados("(11) 3333-4444")).contato());
        assertEquals("11988887777", v.validar(dados("(11) 98888-7777")).contato());
    }

    @Test
    void rejeitaContatoInvalido() {
        for (String s : new String[] {"1", "1234567", "abc", "(00) 98888-7777"})
            assertThrows(ChamadoTiException.class, () -> v.validar(dados(s)));
    }

    @Test
    void rejeitaTextosControlePaginaEStatusInvalidos() {
        assertThrows(ChamadoTiException.class, () -> v.texto("abc\u0000", "Campo", 3, 150, false));
        assertThrows(ChamadoTiException.class, () -> v.paginacao(-1, 8));
        assertThrows(ChamadoTiException.class, () -> v.paginacao(0, 101));
        assertThrows(ChamadoTiException.class, () -> v.status("Em reparo"));
        assertThrows(ChamadoTiException.class, () -> v.validar(null));
    }
}
