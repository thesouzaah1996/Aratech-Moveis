package com.aratech.moveis.rh.funcionarios.service;

import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.dto.Response;

public interface FuncionarioService {

    Response addFuncionario(FuncionarioDTO funcionarioDTO);

    Response getAllFuncionarios();

    Response getAllFuncionarios(int pagina, int tamanho);

    Response getFuncionarioById(Long id);

    Response updateFuncionario(FuncionarioDTO funcionarioDTO, Long id);

    Response enableEmployee(Long id);

    Response disableEmployee(Long id);
}
