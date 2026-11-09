package com.aratech.moveis.config.repository;

import java.util.Collection;

public interface SchemaRepository {
    void registrarPermissoes(Collection<String> permissoes);
}
