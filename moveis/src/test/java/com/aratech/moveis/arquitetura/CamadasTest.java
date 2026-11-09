package com.aratech.moveis.arquitetura;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

class CamadasTest {
    private static final Path FONTES = Path.of("src/main/java/com/aratech/moveis");

    private List<Path> arquivos() throws Exception {
        try (var arquivos = Files.walk(FONTES)) {
            return arquivos.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void persistenciaFicaSomenteNosRepositories() throws Exception {
        var sql =
                Pattern.compile(
                        "\\b(?:SELECT\\s+[^;]+\\s+FROM|INSERT\\s+INTO|DELETE\\s+FROM|UPDATE\\s+\\w+\\s+SET|CREATE\\s+TABLE|ALTER\\s+TABLE|LOCK\\s+TABLE)\\b",
                        Pattern.CASE_INSENSITIVE);
        for (var arquivo : arquivos()) {
            if (arquivo.toString().replace('\\', '/').contains("/repository/")) continue;
            String fonte = Files.readString(arquivo);
            assertFalse(fonte.contains("JdbcTemplate"), arquivo + " depende diretamente de JDBC");
            assertFalse(sql.matcher(fonte).find(), arquivo + " contém SQL fora do repository");
        }
    }

    @Test
    void controllersDependemDeServicosENaoDePersistencia() throws Exception {
        for (var arquivo : arquivos())
            if (arquivo.getFileName().toString().endsWith("Controller.java")) {
                String fonte = Files.readString(arquivo);
                assertFalse(
                        fonte.contains(".repository."), arquivo + " acessa repository diretamente");
                assertFalse(fonte.contains("EntityManager"), arquivo + " acessa JPA diretamente");
            }
    }

    @Test
    void servicosPossuemContratoEImplementacaoSeparados() throws Exception {
        for (var arquivo : arquivos())
            if (arquivo.getFileName().toString().endsWith("Service.java")) {
                String nome = arquivo.getFileName().toString().replace(".java", "");
                assertTrue(
                        Files.readString(arquivo).contains("interface " + nome),
                        arquivo + " deve ser interface");
                Path implementacao = arquivo.getParent().resolve("imp").resolve(nome + "Imp.java");
                assertTrue(Files.exists(implementacao), "Implementação ausente: " + implementacao);
                assertTrue(
                        Files.readString(implementacao).contains("implements " + nome),
                        "Implementação deve cumprir o contrato: " + implementacao);
            }
    }

    @Test
    void repositoriesNaoDependemDeServicos() throws Exception {
        var dependencia =
                Pattern.compile("import\\s+com\\.aratech\\.moveis\\.[\\w.]+\\.service\\.");
        for (var arquivo : arquivos())
            if (arquivo.toString().replace('\\', '/').contains("/repository/"))
                assertFalse(
                        dependencia.matcher(Files.readString(arquivo)).find(),
                        arquivo + " depende de serviço");
    }
}
