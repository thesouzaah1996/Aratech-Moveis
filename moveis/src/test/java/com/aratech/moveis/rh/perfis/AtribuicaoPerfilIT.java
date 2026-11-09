package com.aratech.moveis.rh.perfis;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.rh.perfis.exception.PerfilException;
import com.aratech.moveis.rh.perfis.service.AtribuicaoPerfilService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@Transactional
class AtribuicaoPerfilIT {
    @Autowired AtribuicaoPerfilService service;
    @Autowired JdbcTemplate jdbc;

    private Long usuario(String nome) {
        return jdbc.queryForObject(
                "INSERT INTO funcionario (nome_completo, email, setor) VALUES (?, ?, 'RH')"
                        + " RETURNING id",
                Long.class,
                nome,
                UUID.randomUUID() + "@example.test");
    }

    private String perfil() {
        String nome = "PERFIL TESTE " + UUID.randomUUID();
        jdbc.update("INSERT INTO perfil_acesso (nome) VALUES (?)", nome);
        return nome;
    }

    @Test
    void atribuiPorNomeSemDuplicarEPreservaPerfisAnteriores() {
        Long id = usuario("Usuário " + UUID.randomUUID());
        String primeiro = perfil(), segundo = perfil();
        assertEquals(1, service.atribuir(id, "  " + primeiro.toLowerCase() + "  ").perfis().size());
        assertEquals(1, service.atribuir(id, primeiro).perfis().size());
        var atualizado = service.atribuir(id, segundo);
        assertEquals(2, atualizado.perfis().size());
        assertTrue(atualizado.perfis().stream().anyMatch(p -> p.nome().equals(primeiro)));
        assertEquals(
                2L,
                jdbc.queryForObject(
                        "SELECT count(*) FROM funcionario_perfil_acesso WHERE funcionario_id = ?",
                        Long.class,
                        id));
    }

    @Test
    void buscaPorNomeComPaginacaoERetornaPerfisPersistidos() {
        String nome = "BuscaTeste " + UUID.randomUUID();
        Long primeiro = usuario(nome + " A");
        usuario(nome + " B");
        String perfil = perfil();
        service.atribuir(primeiro, perfil);
        var pagina = service.buscar(nome.toLowerCase(), 0, 1);
        assertEquals(2, pagina.getTotalElements());
        assertEquals(2, pagina.getTotalPages());
        assertEquals(primeiro, pagina.getContent().getFirst().id());
        assertEquals(perfil, pagina.getContent().getFirst().perfis().getFirst().nome());
        assertNotEquals(primeiro, service.buscar(nome, 1, 1).getContent().getFirst().id());
    }

    @Test
    void rejeitaUsuarioOuPerfilInexistenteSemCriarVinculo() {
        Long id = usuario("Sem perfil " + UUID.randomUUID());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(
                                PerfilException.class,
                                () -> service.atribuir(id, "INEXISTENTE " + UUID.randomUUID()))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(
                                PerfilException.class,
                                () -> service.atribuir(Long.MAX_VALUE, "OPERADOR"))
                        .getStatus());
        assertEquals(
                0L,
                jdbc.queryForObject(
                        "SELECT count(*) FROM funcionario_perfil_acesso WHERE funcionario_id = ?",
                        Long.class,
                        id));
        assertThrows(PerfilException.class, () -> service.buscar("Ana", -1, 8));
        assertThrows(PerfilException.class, () -> service.atribuir(id, ""));
    }

    @Test
    void buscaNaoInterpretaCuringasComoParteDoComando() {
        String nome = "Busca_100% " + UUID.randomUUID();
        Long id = usuario(nome);
        assertEquals(id, service.buscar(nome, 0, 8).getContent().getFirst().id());
        assertEquals(0, service.buscar("' OR 1=1 --", 0, 8).getTotalElements());
    }

    @Test
    void buscaPorIdEnquantoDigitaSemConverterEntradaParaNumero() {
        Long id = usuario("Busca por identificador " + UUID.randomUUID());
        String termo = id.toString();
        var resultados = service.buscar(termo, 0, 100);
        assertTrue(resultados.getContent().stream().anyMatch(u -> u.id().equals(id)));
        String prefixo = termo.substring(0, 1);
        assertTrue(
                service.buscar(prefixo, 0, 100).getContent().stream()
                        .allMatch(
                                u ->
                                        u.id().toString().startsWith(prefixo)
                                                || u.nome().contains(prefixo)));
        assertEquals(0, service.buscar("9".repeat(100), 0, 8).getTotalElements());
    }
}
