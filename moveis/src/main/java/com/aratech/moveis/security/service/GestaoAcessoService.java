package com.aratech.moveis.security.service;

import com.aratech.moveis.security.dto.UsuarioSessao;

import java.util.*;

public interface GestaoAcessoService {

    void validarConvite(Long id);

    void validarMudancaEmail(Long id, String novoEmail);

    void validarAlteracaoFuncionario(Long id, boolean desativar);

    UsuarioSessao atuante();

    Set<String> consultar(Long perfilId);

    void validarPerfilPorNome(String nome);

    Set<String> salvar(Long perfilId, Set<String> permissoes);

    void remover(Long usuarioId, Long perfilId);
}
