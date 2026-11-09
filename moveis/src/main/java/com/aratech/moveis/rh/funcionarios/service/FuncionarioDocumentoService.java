package com.aratech.moveis.rh.funcionarios.service;

import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;

import java.util.Map;

public interface FuncionarioDocumentoService {

    Map<String, String> upload(FuncionarioDTO dto);

    Map<String, String> uploadAtualizacao(FuncionarioDTO dto);

    void removerAposConfirmacao(Map<String, String> documentos);

    void remover(Map<String, String> documentos);
}
