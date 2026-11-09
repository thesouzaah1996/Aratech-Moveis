package com.aratech.moveis.portaria.controleacesso;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.exception.ControleAcessoException;
import com.aratech.moveis.portaria.controleacesso.validation.ChegadaValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.UUID;

class ChegadaValidatorTest {
    final ChegadaValidator validator = new ChegadaValidator();

    ChegadaDTO dto(
            String nf,
            String empresa,
            String motorista,
            String placa,
            String descricao,
            String setor) {
        return new ChegadaDTO(
                nf,
                empresa,
                motorista,
                placa,
                descricao,
                setor,
                UUID.randomUUID(),
                "CARREGAMENTO".equals(setor) ? "DESCARREGAR" : null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC1234", "abc-1234", "ABC1D23"})
    void aceitaPlacaAntigaEMercosul(String placa) {
        assertEquals(
                7,
                validator
                        .validar(
                                dto("123", "Empresa", "Motorista", placa, "Carga", "ALMOXARIFADO"),
                                true)
                        .placa()
                        .length());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "123", "ABCD123", "ABC_1234", "ABC12X4", "../ABC1234"})
    void rejeitaPlacasInvalidas(String placa) {
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto("123", "Empresa", "Motorista", placa, "Carga", "ALMOXARIFADO"),
                                true));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                " ",
                "NF123",
                "1/2",
                "123\n456",
                "0",
                "000000000",
                "1234567890",
                "3526080629813400011755005000062424117866468",
                "35260806298134000117550050000624241178664683",
                "00000000000000000000000000000000000000000000"
            })
    void rejeitaNotasInvalidas(String nf) {
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto(nf, "Empresa", "Motorista", "ABC1234", "Carga", "ALMOXARIFADO"),
                                true));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "1",
                "000000001",
                "999999999",
                "35260806298134000117550050000624241178664682"
            })
    void aceitaNumeroOuChaveComDigitoValido(String nf) {
        assertEquals(
                nf,
                validator
                        .validar(
                                dto(nf, "Empresa", "Motorista", "ABC1D23", "Carga", "ALMOXARIFADO"),
                                true)
                        .notaFiscal());
    }

    @Test
    void camposObrigatoriosELimites() {
        assertThrows(ControleAcessoException.class, () -> validator.validar(null, true));
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto("123", null, "Motorista", "ABC1234", "Carga", "ALMOXARIFADO"),
                                true));
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto("123", "Empresa", " ", "ABC1234", "Carga", "ALMOXARIFADO"),
                                true));
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto("123", "Empresa", "Motorista", "ABC1234", " ", "ALMOXARIFADO"),
                                true));
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto(
                                        "123",
                                        "A".repeat(151),
                                        "Motorista",
                                        "ABC1234",
                                        "Carga",
                                        "ALMOXARIFADO"),
                                true));
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto(
                                        "123",
                                        "Empresa",
                                        "Motorista",
                                        "ABC1234",
                                        "A".repeat(1001),
                                        "ALMOXARIFADO"),
                                true));
    }

    @Test
    void rejeitaCaracteresDeControleEInvisiveis() {
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto(
                                        "123",
                                        "Empresa\nTeste",
                                        "Motorista",
                                        "ABC1234",
                                        "Carga",
                                        "ALMOXARIFADO"),
                                true));
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto(
                                        "123",
                                        "Empresa",
                                        "Motorista\u200BTeste",
                                        "ABC1234",
                                        "Carga",
                                        "ALMOXARIFADO"),
                                true));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"PCP", "OUTRO"})
    void outrosSetoresAindaNaoIntegrados(String setor) {
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                dto("123", "Empresa", "Motorista", "ABC1234", "Carga", setor),
                                true));
    }

    @Test
    void aceitaCarregamento() {
        assertEquals(
                "CARREGAMENTO",
                validator
                        .validar(
                                dto(
                                        "123",
                                        "Empresa",
                                        "Motorista",
                                        "ABC1234",
                                        "Carga",
                                        "CARREGAMENTO"),
                                true)
                        .setorResponsavel());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"OUTRO", "carregar"})
    void carregamentoExigeOperacaoValida(String operacao) {
        assertThrows(
                ControleAcessoException.class,
                () ->
                        validator.validar(
                                new ChegadaDTO(
                                        "123",
                                        "Empresa",
                                        "Motorista",
                                        "ABC1234",
                                        "Carga",
                                        "CARREGAMENTO",
                                        UUID.randomUUID(),
                                        operacao),
                                true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CARREGAR", "DESCARREGAR"})
    void preservaOperacao(String operacao) {
        assertEquals(
                operacao,
                validator
                        .validar(
                                new ChegadaDTO(
                                        "123",
                                        "Empresa",
                                        "Motorista",
                                        "ABC1234",
                                        "Carga",
                                        "CARREGAMENTO",
                                        UUID.randomUUID(),
                                        operacao),
                                true)
                        .tipoOperacao());
    }

    @Test
    void exigeChaveSomenteNoCadastro() {
        var d =
                new ChegadaDTO(
                        "123", "Empresa", "Motorista", "ABC1234", "Carga", "ALMOXARIFADO", null);
        assertThrows(ControleAcessoException.class, () -> validator.validar(d, true));
        assertNotNull(validator.validar(d, false));
    }
}
