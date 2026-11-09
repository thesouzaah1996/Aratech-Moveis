package com.aratech.moveis.security.service.imp;

import com.aratech.moveis.security.exception.ExcessoTentativas;
import com.aratech.moveis.security.repository.LimiteLoginRepository;
import com.aratech.moveis.security.service.LimiteLoginService;
import com.aratech.moveis.security.validation.IdentificadorHash;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

import java.security.*;
import java.time.*;

@Service
@RequiredArgsConstructor
public class LimiteLoginServiceImp implements LimiteLoginService {
    private final LimiteLoginRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = ExcessoTentativas.class)
    public void consumir(String tipo, String valor, int limite) {
        String chave = tipo + ":" + IdentificadorHash.hash(valor);
        repository.removerTentativasAntigas();
        repository.inserirTentativa(chave);
        var janela = repository.bloquearTentativa(chave);
        Instant inicio = janela.janela();
        int contagem = janela.contagem();
        if (inicio.plus(Duration.ofMinutes(15)).isBefore(Instant.now())) {
            repository.reiniciarJanela(chave);
        } else {
            if (contagem >= limite) throw new ExcessoTentativas();
            repository.incrementarTentativas(chave);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void limparConta(String email) {
        repository.removerTentativaDaConta("conta:" + IdentificadorHash.hash(email));
    }
}
