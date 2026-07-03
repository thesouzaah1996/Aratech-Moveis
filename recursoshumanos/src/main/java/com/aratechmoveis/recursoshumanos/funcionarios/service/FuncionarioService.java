package com.aratechmoveis.recursoshumanos.funcionarios.service;

import com.aratechmoveis.recursoshumanos.funcionarios.dto.AtribuirPerfisDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.dto.FuncionarioDTO;
import com.aratechmoveis.recursoshumanos.response.Response;

public interface FuncionarioService {

    Response addFuncionario(FuncionarioDTO funcionario);

    Response getFuncionarios();

    Response getFuncionariosPorNome(String nome);

    Response updateFuncionario(Long id, FuncionarioDTO funcionarioDTO);

    Response enableFuncionario(Long id);

    Response disableFuncionario(Long id);

    Response atribuirPerfis(Long id, AtribuirPerfisDTO atribuirPerfisDTO);
}
