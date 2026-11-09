package com.aratech.moveis.security.repository;

public interface ProvisionarAdministradorTemporarioRepository {
    void bloquearAdministradorParaBootstrap();

    Boolean existeAdministrador();

    int inserirAdministrador(String loginValidado, String encode);
}
