package com.aratechmoveis.manutencao.pecaestoque.service;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.pecaestoque.dto.PecaEstoqueDTO;

public interface PecaEstoqueService {
    Response adicionarPecaEstoque(PecaEstoqueDTO pecaEstoqueDTO);
    Response atualizarPecaEstoque(Long id, PecaEstoqueDTO pecaEstoqueDTO);
    Response listarPecasEstoque();
    Response buscarPecaEstoquePorId(Long id);
    Response removerPecaEstoque(Long id);
}
