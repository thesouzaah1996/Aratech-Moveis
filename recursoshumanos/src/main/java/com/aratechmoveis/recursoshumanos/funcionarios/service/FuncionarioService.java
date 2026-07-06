package com.aratechmoveis.recursoshumanos.funcionarios.service;

import com.aratechmoveis.recursoshumanos.funcionarios.dto.AtribuirPerfisDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.dto.FuncionarioDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.dto.LoginFuncionarioDTO;
import com.aratechmoveis.recursoshumanos.response.Response;

public interface FuncionarioService {

    Response adicionarFuncionario(FuncionarioDTO funcionario);

    Response listarFuncionarios();

    Response buscarFuncionariosPorNome(String nome);

    Response atualizarFuncionario(Long id, FuncionarioDTO funcionarioDTO);

    Response ativarFuncionario(Long id);

    Response desativarFuncionario(Long id);

    Response atribuirPerfis(Long id, AtribuirPerfisDTO atribuirPerfisDTO);

    Response atribuirEmailCorporativo(LoginFuncionarioDTO loginFuncionarioDTO);
}
