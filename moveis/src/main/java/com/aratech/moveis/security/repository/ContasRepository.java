package com.aratech.moveis.security.repository;

import com.aratech.moveis.security.dto.Credencial;
import com.aratech.moveis.security.model.DadosConta;

import java.util.*;
import java.util.List;

public interface ContasRepository {
    List<Credencial> consultarAdministrador(String email);

    List<Credencial> consultarConta(String email);

    List<DadosConta> consultarAdministradorAtivo();

    List<DadosConta> consultarFuncionarioAtivo(Long id);

    List<String> consultarPerfil(Long id);

    List<String> consultarPermissao(Long id);
}
