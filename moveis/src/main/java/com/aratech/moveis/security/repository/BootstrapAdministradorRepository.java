package com.aratech.moveis.security.repository;

import java.util.List;

public interface BootstrapAdministradorRepository {
    void bloquearContasParaBootstrap();

    Boolean administradorConfigurado();

    List<String> buscarLoginDoFuncionarioAtivoComBloqueio(Long usuarioId);

    List<Long> bloquearFuncionario(Long usuarioId);

    int inserirConta(Long usuarioId, String encode);
}
