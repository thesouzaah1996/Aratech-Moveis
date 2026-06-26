package com.aratechmoveis.almoxarifado.fornecedor.service;

import com.aratechmoveis.almoxarifado.res.Response;
import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorDTO;

public interface FornecedorService {
    Response addFornecedor(FornecedorDTO fornecedorDTO);
    Response updateFornecedor(Long id, FornecedorDTO fornecedorDTO);
    Response getFornecedores();
    Response disableFornecedor(Long id);
    Response enableFornecedor(Long id);
    Response lookupFornecedor();
}
