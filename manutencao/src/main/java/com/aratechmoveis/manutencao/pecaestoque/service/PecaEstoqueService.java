package com.aratechmoveis.manutencao.pecaestoque.service;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.pecaestoque.dto.EntradaEstoqueDTO;
import com.aratechmoveis.manutencao.pecaestoque.dto.PecaEstoqueDTO;
import com.aratechmoveis.manutencao.pecaestoque.dto.SaidaEstoqueDTO;

public interface PecaEstoqueService {
    Response adicionarPecaEstoque(PecaEstoqueDTO pecaEstoqueDTO);
    Response atualizarPecaEstoque(Long id, PecaEstoqueDTO pecaEstoqueDTO);
    Response listarPecasEstoque();
    Response buscarPecaEstoquePorId(Long id);
    Response removerPecaEstoque(Long id);
    Response entradaEstoque(String codigo, EntradaEstoqueDTO entradaEstoqueDTO);
    Response saidaEstoque(String codigo, SaidaEstoqueDTO saidaEstoqueDTO);
}
