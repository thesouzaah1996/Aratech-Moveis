package com.aratechmoveis.login.auth.service;

import com.aratechmoveis.login.auth.repository.CodigoResetSenhaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class GeradorCodigo {

    private final CodigoResetSenhaRepo codigoResetSenhaRepo;

    private static final String ALFANUMERICO  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TAMANHO_CODIGO = 5;

    public String gerarCodigoUnico() {
        String codigo;
        do {
            codigo = gerarCodigoAleatorio();
        } while (codigoResetSenhaRepo.findByCode(codigo).isPresent());

        return codigo;
    }

    private String gerarCodigoAleatorio() {
        StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            int index = random.nextInt(ALFANUMERICO.length());
            sb.append(ALFANUMERICO.charAt(index));
        }
        return sb.toString();
    }
}
