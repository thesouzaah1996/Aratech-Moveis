package com.aratech.moveis.almoxarifado.produtos.repository;

import com.aratech.moveis.almoxarifado.produtos.model.Produto;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    boolean existsBySkuIgnoreCase(String sku);

    @EntityGraph(attributePaths = {"categoria", "fornecedor"})
    @Query(
            "select p from Produto p where p.ativo = true and (:busca = '' or lower(p.nome) like"
                    + " lower(concat('%', :busca, '%')) escape '!' or lower(p.sku) like"
                    + " lower(concat('%', :busca, '%')) escape '!' or lower(p.descricao) like"
                    + " lower(concat('%', :busca, '%')) escape '!')")
    Page<Produto> buscar(@Param("busca") String busca, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> findByIdForUpdate(@Param("id") Long id);
}
