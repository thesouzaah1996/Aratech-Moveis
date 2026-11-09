package com.aratech.moveis.security.repository;

import java.util.*;
import java.util.List;

public interface SenhasRepository {
    List<String> buscarEmailDoFuncionarioAtivoComBloqueio(Long id);

    Boolean existeConta(Long id);

    Object bloquearIdentificadorLogin(String login);

    Boolean loginOcupado(String login, Long id, String login2);

    int inserirConta(Long id, String encode, String corporativo);

    List<String> buscarHashDoAdministradorComBloqueio();

    int alterarSenhaDoAdministrador(String encode);

    List<String> buscarHashDaContaComBloqueio(Long id);

    int alterarSenhaDaConta(String encode, Long id);
}
