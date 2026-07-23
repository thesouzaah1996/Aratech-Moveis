package com.aratechmoveis.login.auth.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TentativasLoginService {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration BLOQUEIO = Duration.ofMinutes(15);

    private final Cache<String, Integer> tentativas = Caffeine.newBuilder()
            .expireAfterWrite(BLOQUEIO)
            .build();

    private final Cache<String, Boolean> bloqueados = Caffeine.newBuilder()
            .expireAfterWrite(BLOQUEIO)
            .build();

    public boolean isBloqueado(String email) {
        return Boolean.TRUE.equals(bloqueados.getIfPresent(email));
    }

    public void registrarFalha(String email) {
        int count = tentativas.asMap().merge(email, 1, Integer::sum);
        if (count >= MAX_TENTATIVAS) {
            bloqueados.put(email, true);
        }
    }

    public void registrarSucesso(String email) {
        tentativas.invalidate(email);
        bloqueados.invalidate(email);
    }
}
