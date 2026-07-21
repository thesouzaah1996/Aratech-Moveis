package com.aratechmoveis.login.auth.service;

import com.aratechmoveis.login.auth.entity.CodigoResetSenha;
import com.aratechmoveis.login.auth.repository.CodigoResetSenhaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeast;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeradorCodigo")
class GeradorCodigoTest {

    @Mock
    private CodigoResetSenhaRepo codigoResetSenhaRepo;

    @InjectMocks
    private GeradorCodigo geradorCodigo;

    @Nested
    @DisplayName("gerarCodigoUnico")
    class GerarCodigoUnico {

        @Test
        @DisplayName("deve gerar um código alfanumérico de 5 caracteres")
        void deveGerarCodigoDeCincoCaracteres() {
            given(codigoResetSenhaRepo.findByCodigo(any())).willReturn(Optional.empty());

            String codigo = geradorCodigo.gerarCodigoUnico();

            assertThat(codigo).hasSize(5);
            assertThat(codigo).matches("[A-Z0-9]{5}");
        }

        @Test
        @DisplayName("deve gerar um novo código quando o primeiro já está em uso")
        void deveGerarNovoCodigoQuandoPrimeiroJaEmUso() {
            given(codigoResetSenhaRepo.findByCodigo(any()))
                    .willReturn(Optional.of(CodigoResetSenha.builder().build()), Optional.empty());

            String codigo = geradorCodigo.gerarCodigoUnico();

            assertThat(codigo).hasSize(5);
            then(codigoResetSenhaRepo).should(atLeast(2)).findByCodigo(any());
        }
    }
}
