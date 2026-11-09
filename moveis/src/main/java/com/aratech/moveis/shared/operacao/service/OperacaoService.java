package com.aratech.moveis.shared.operacao.service;

import com.aratech.moveis.security.dto.UsuarioSessao;

import java.util.*;
import java.util.function.Supplier;

public interface OperacaoService {

    UsuarioSessao autor();

    <T> T comando(UUID chave, String escopo, Object dto, Class<T> tipo, Supplier<T> acao);

    void auditar(String modulo, String acao, String entidade, Long id, String detalhe);

    void auditar(
            UsuarioSessao u, String modulo, String acao, String entidade, Long id, String detalhe);

    void versao(long atual, Long recebida);

    void exigir(boolean condicao, String mensagem);

    String motivo(String valor);

    int parametro(String chave);
}
