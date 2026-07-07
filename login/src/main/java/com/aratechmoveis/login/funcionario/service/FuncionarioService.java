package com.aratechmoveis.login.funcionario.service;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.response.Response;
import com.aratechmoveis.login.funcionario.dto.FuncionarioDTO;

public interface FuncionarioService {

    Response adicionarFuncionario(Funcionario funcionario);

    Response editarFuncionario(FuncionarioDTO funcionario);

}
