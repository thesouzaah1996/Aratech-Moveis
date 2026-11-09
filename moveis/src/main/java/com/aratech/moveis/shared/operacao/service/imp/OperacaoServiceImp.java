package com.aratech.moveis.shared.operacao.service.imp;

import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.operacao.repository.OperacaoRepository;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class OperacaoServiceImp implements OperacaoService {
    private final OperacaoRepository repository;
    final JsonMapper mapper;

    public UsuarioSessao autor() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof UsuarioSessao u))
            throw new AuthException(401, "Entre no sistema para continuar.");
        return u;
    }

    public <T> T comando(UUID chave, String escopo, Object dto, Class<T> tipo, Supplier<T> acao) {
        if (chave == null) throw new AuthException(400, "Informe a chave da operação.");
        String assinatura;
        try {
            assinatura =
                    HexFormat.of()
                            .formatHex(
                                    MessageDigest.getInstance("SHA-256")
                                            .digest(
                                                    (autor().id() + ":" + dto)
                                                            .getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        repository.inserirComando(chave, escopo, assinatura);
        var linha = repository.bloquearComando(chave);
        if (!escopo.equals(linha.escopo()) || !assinatura.equals(linha.assinatura()))
            throw new AuthException(
                    409, "Esta chave já identifica outra operação. Confira o envio.");
        if (linha.resposta() != null) return mapper.readValue(linha.resposta().toString(), tipo);
        T resultado = acao.get();
        repository.atualizarComando(mapper.writeValueAsString(resultado), chave);
        return resultado;
    }

    public void auditar(String modulo, String acao, String entidade, Long id, String detalhe) {
        auditar(autor(), modulo, acao, entidade, id, detalhe);
    }

    public void auditar(
            UsuarioSessao u, String modulo, String acao, String entidade, Long id, String detalhe) {
        repository.inserirAuditoria(modulo, acao, entidade, id, u.id(), u.nome(), detalhe);
    }

    public void versao(long atual, Long recebida) {
        if (recebida == null || atual != recebida)
            throw new AuthException(
                    409,
                    "Registro atualizado por outra pessoa. Reabra a operação com os dados atuais.");
    }

    public void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new AuthException(409, mensagem);
    }

    public String motivo(String valor) {
        if (valor == null || valor.trim().length() < 3 || valor.trim().length() > 1000)
            throw new AuthException(400, "Informe um motivo de 3 a 1000 caracteres.");
        return valor.trim();
    }

    public int parametro(String chave) {
        return Objects.requireNonNull(repository.consultarParametro(chave));
    }
}
