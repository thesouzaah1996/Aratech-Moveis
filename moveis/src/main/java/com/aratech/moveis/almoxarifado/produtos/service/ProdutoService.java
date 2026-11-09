package com.aratech.moveis.almoxarifado.produtos.service;

import com.aratech.moveis.almoxarifado.produtos.dto.MovimentacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.ProdutoAtualizacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.ProdutoDTO;
import com.aratech.moveis.almoxarifado.produtos.dto.Response;

public interface ProdutoService {
    Response addProduto(ProdutoDTO dto);

    Response getAllProdutos();

    Response getAllProdutos(int pagina, int tamanho, String busca);

    Response getProdutoById(Long id);

    Response updateProduto(ProdutoAtualizacaoDTO dto, Long id);

    Response deleteProduto(Long id);

    Response entrada(Long id, MovimentacaoDTO dto);

    Response saida(Long id, MovimentacaoDTO dto);

    Response getMovimentacoes(Long id, int pagina, int tamanho);
}
