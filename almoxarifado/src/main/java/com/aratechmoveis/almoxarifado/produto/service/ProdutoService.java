package com.aratechmoveis.almoxarifado.produto.service;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;

public interface ProdutoService {
    Response adicionarProduto(ProdutoDTO produtoDTO);
    Response atualizarProduto(Long id, ProdutoDTO produtoDTO);
    Response listarProdutos();
    Response buscarProdutoPorId(Long id);
    Response removerProduto(Long id);
}
