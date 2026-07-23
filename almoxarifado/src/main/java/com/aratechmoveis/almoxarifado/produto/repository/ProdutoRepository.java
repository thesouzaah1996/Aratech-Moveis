package com.aratechmoveis.almoxarifado.produto.repository;

import com.aratechmoveis.almoxarifado.produto.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    boolean existsBySku(String sku);
    Optional<Produto> findBySku(String sku);
    boolean existsByLocalArmazenamentoAndIdNot(String localArmazenamento, Long id);
}
