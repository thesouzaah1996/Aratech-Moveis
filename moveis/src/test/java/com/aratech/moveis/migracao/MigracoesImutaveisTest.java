package com.aratech.moveis.migracao;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

class MigracoesImutaveisTest {
    @Test
    void preservaAsTrezeMigracoesJaAplicadas() throws Exception {
        try (var manifesto = getClass().getResourceAsStream("/db/migration-checksums.sha256")) {
            assertNotNull(manifesto);
            var linhas = new String(manifesto.readAllBytes(), StandardCharsets.UTF_8).lines().filter(l -> !l.isBlank()).toList();
            assertEquals(13, linhas.size());
            for (String linha : linhas) {
                String[] partes = linha.split("  ", 2);
                try (var arquivo = getClass().getResourceAsStream("/db/migration/" + partes[1])) {
                    assertNotNull(arquivo, partes[1]);
                    byte[] normalizado = new String(arquivo.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8);
                    assertEquals(partes[0], HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(normalizado)),
                            "Migração aplicada foi alterada: " + partes[1] + ". Crie uma nova versão.");
                }
            }
        }
    }
}
