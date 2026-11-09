package com.aratech.moveis.almoxarifado.produtos.repository;

import com.aratech.moveis.almoxarifado.produtos.model.MovimentacaoEstoque;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    Optional<MovimentacaoEstoque> findByChaveOperacao(UUID chaveOperacao);

    Page<MovimentacaoEstoque> findByProdutoId(Long produtoId, Pageable pageable);
}
