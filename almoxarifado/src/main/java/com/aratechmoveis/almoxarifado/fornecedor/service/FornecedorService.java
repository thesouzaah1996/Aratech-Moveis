package com.aratechmoveis.almoxarifado.fornecedor.service;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorDTO;

public interface FornecedorService {
    Response adicionarFornecedor(FornecedorDTO fornecedorDTO);
    Response atualizarFornecedor(Long id, FornecedorDTO fornecedorDTO);
    Response listarFornecedores();
    Response desativarFornecedor(Long id);
    Response ativarFornecedor(Long id);
    Response buscarOpcoesFornecedor();
}
