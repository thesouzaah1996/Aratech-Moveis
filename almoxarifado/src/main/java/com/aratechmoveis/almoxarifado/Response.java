package com.aratechmoveis.almoxarifado;

import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaDTO;
import com.aratechmoveis.almoxarifado.categoria.dto.CategoriaLookupDTO;
import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorDTO;
import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorLookupDTO;
import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private int status;
    private String message;

    private String token;
    private String expirationTime;

    private FornecedorDTO fornecedorDTO;
    private List<FornecedorDTO> fornecedores;

    private CategoriaDTO categoria;
    private List<CategoriaDTO> categorias;

    private ProdutoDTO produto;
    private List<ProdutoDTO> produtos;

    private List<FornecedorLookupDTO> fornecedorLookup;
    private List<CategoriaLookupDTO> categoriaLookup;

    private final LocalDateTime timestamp = LocalDateTime.now();
}
