package com.aratechmoveis.almoxarifado.categoria.service;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaDTO;

public interface CategoriaService {
    Response criarCategoria(CategoriaDTO categoriaDTO);
    Response listarCategorias();
    Response atualizarCategoria(Long id, CategoriaDTO categoriaDTO);
    Response removerCategoria(Long id);
    Response buscarOpcoesCategoria();
}
