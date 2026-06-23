package com.aratechmoveis.almoxarifado.categoria.service;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaDTO;

public interface CategoriaService {
    Response criarCategoria(CategoriaDTO categoriaDTO);
    Response getCategorias();
    Response updateCategoria(Long id, CategoriaDTO categoriaDTO);
    Response deleteCategoria(Long id);
    Response lookupCategoria();
}
