package com.aratechmoveis.almoxarifado.produto.repository;

import com.aratechmoveis.almoxarifado.produto.modelo.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    boolean existsBySku(String sku);
    boolean existsByLocalArmazenamento(String localArmazenamento);
    boolean existsByLocalArmazenamentoAndIdNot(String localArmazenamento, Long id);
}
