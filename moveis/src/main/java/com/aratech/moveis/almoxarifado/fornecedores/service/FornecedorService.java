package com.aratech.moveis.almoxarifado.fornecedores.service;

import com.aratech.moveis.almoxarifado.fornecedores.dto.FornecedorDTO;
import com.aratech.moveis.almoxarifado.fornecedores.dto.Response;

public interface FornecedorService {

    Response addFornecedor(FornecedorDTO fornecedorDTO);

    Response getAllFornecedores();

    Response getAllFornecedores(int pagina, int tamanho);

    Response getAllFornecedores(int pagina, int tamanho, String busca, Boolean ativo);

    Response getFornecedorById(Long id);

    Response updateFornecedor(FornecedorDTO fornecedorDTO, Long id);

    Response enableFornecedor(Long id);

    Response disableFornecedor(Long id);
}
