package com.aratechmoveis.manutencao.pecaestoque.service;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.pecaestoque.dto.PecaEstoqueDTO;

public interface PecaEstoqueService {
    Response addPecaEstoque(PecaEstoqueDTO pecaEstoqueDTO);
    Response updatePecaEstoque(Long id, PecaEstoqueDTO pecaEstoqueDTO);
    Response getPecasEstoque();
    Response getPecaEstoqueById(Long id);
    Response deletePecaEstoque(Long id);
}
