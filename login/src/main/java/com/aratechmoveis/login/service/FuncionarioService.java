package com.aratechmoveis.login.service;

import com.aratechmoveis.login.response.Response;
import com.aratechmoveis.login.dto.FuncionarioDTO;

public interface FuncionarioService {

    Response adicionarFuncionario(FuncionarioDTO funcionario);

    Response editarFuncionario(FuncionarioDTO funcionario);

}
