package com.aratech.moveis.config.repository.imp;

import com.aratech.moveis.config.repository.SchemaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class SchemaRepositoryImp implements SchemaRepository {
    private final JdbcTemplate jdbc;

    public void registrarPermissoes(Collection<String> permissoes) {
        for (String codigo : permissoes)
            jdbc.update(
                    "INSERT INTO acesso_permissao(codigo) VALUES (?) ON CONFLICT DO NOTHING",
                    codigo);
    }
}
