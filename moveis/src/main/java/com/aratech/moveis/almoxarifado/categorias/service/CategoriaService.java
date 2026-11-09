package com.aratech.moveis.almoxarifado.categorias.service;

import com.aratech.moveis.almoxarifado.categorias.dto.CategoriaDTO;
import com.aratech.moveis.almoxarifado.categorias.dto.Response;

public interface CategoriaService {

    Response addCategoria(CategoriaDTO categoriaDTO);

    Response getAllCategorias();

    Response getAllCategorias(int pagina, int tamanho);

    Response getAllCategorias(int pagina, int tamanho, String nome, Boolean ativo);

    Response getCategoriaById(Long id);

    Response updateCategoria(CategoriaDTO categoriaDTO, Long id);

    Response enableCategoria(Long id);

    Response disableCategoria(Long id);
}
