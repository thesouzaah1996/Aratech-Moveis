package com.aratech.moveis.almoxarifado.produtos.dto;

import java.time.LocalDateTime;

public record ProdutoResumoDTO(
        Long id,
        String nome,
        String sku,
        Integer quantidade,
        Long categoriaID,
        String categoriaNome,
        Long fornecedorID,
        String fornecedorNome,
        String localArmazenamento,
        String descricao,
        LocalDateTime vencimentoProduto,
        LocalDateTime criadoEm,
        Boolean ativo,
        Integer quantidadeMinima) {}
