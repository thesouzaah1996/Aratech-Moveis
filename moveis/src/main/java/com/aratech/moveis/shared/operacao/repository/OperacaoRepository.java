package com.aratech.moveis.shared.operacao.repository;

import com.aratech.moveis.shared.operacao.model.ComandoPersistido;

import org.springframework.jdbc.core.*;

import java.util.*;
import java.util.UUID;

public interface OperacaoRepository {
    int inserirComando(UUID chave, String escopo, String assinatura);

    ComandoPersistido bloquearComando(UUID chave);

    int atualizarComando(String writeValueAsString, UUID chave);

    int inserirAuditoria(
            String modulo,
            String acao,
            String entidade,
            Long id,
            Long id2,
            String nome,
            String detalhe);

    Integer consultarParametro(String chave);
}
