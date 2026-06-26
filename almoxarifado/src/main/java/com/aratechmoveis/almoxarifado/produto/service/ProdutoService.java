package com.aratechmoveis.almoxarifado.produto.service;

import com.aratechmoveis.almoxarifado.res.Response;
import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;

public interface ProdutoService {
    Response addProduto(ProdutoDTO produtoDTO);
    Response updateProduto(Long id, ProdutoDTO produtoDTO);
    Response getProdutos();
    Response getProdutoById(Long id);
    Response deleteProduto(Long id);
}
