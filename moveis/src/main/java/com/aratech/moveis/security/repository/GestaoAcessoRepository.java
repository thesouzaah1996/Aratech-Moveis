package com.aratech.moveis.security.repository;

import java.util.*;
import java.util.List;

public interface GestaoAcessoRepository {
    Boolean administrador(Long id);

    List<String> listarPermissoesDoUsuario(Long id);

    List<String> buscarEmailPessoal(Long id);

    Boolean usaEmailPessoalComoLogin(Long id);

    Long contarAdministradoresAtivos();

    Boolean existePerfil(Long perfilId);

    List<String> listarPermissoesDoPerfil(Long perfilId);

    List<Long> bloquearPerfilPorNome(String replaceAll);

    List<Long> bloquearPerfilPorId(Long perfilId);

    int removerPermissoesDoPerfil(Long perfilId);

    int inserirPermissao(Long perfilId, String permissao);

    int removerVinculo(Long usuarioId, Long perfilId);
}
