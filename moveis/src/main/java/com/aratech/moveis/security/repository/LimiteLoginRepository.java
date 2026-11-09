package com.aratech.moveis.security.repository;

import com.aratech.moveis.security.model.TentativaLogin;

public interface LimiteLoginRepository {
    int removerTentativasAntigas();

    int inserirTentativa(String chave);

    TentativaLogin bloquearTentativa(String chave);

    int reiniciarJanela(String chave);

    int incrementarTentativas(String chave);

    int removerTentativaDaConta(String valor);
}
