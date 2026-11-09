package com.aratech.moveis.security.config;

import com.aratech.moveis.config.repository.SchemaRepository;
import com.aratech.moveis.security.authorization.Permissoes;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@org.springframework.core.annotation.Order(30)
public class InicializarSeguranca implements ApplicationRunner {
    private final SchemaRepository repository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        repository.registrarPermissoes(Permissoes.catalogo());
    }
}
