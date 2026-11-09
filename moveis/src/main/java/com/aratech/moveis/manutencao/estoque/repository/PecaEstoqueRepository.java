package com.aratech.moveis.manutencao.estoque.repository;

import com.aratech.moveis.manutencao.estoque.model.PecaEstoque;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PecaEstoqueRepository extends JpaRepository<PecaEstoque, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);

    @Query(
            "select p from PecaEstoque p where p.ativo=true and (:busca='' or lower(p.nome) like"
                    + " lower(concat('%',:busca,'%')) escape '!' or lower(p.codigo) like"
                    + " lower(concat('%',:busca,'%')) escape '!' or lower(p.localizacao) like"
                    + " lower(concat('%',:busca,'%')) escape '!' or lower(p.descricao) like"
                    + " lower(concat('%',:busca,'%')) escape '!')")
    Page<PecaEstoque> buscar(@Param("busca") String busca, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PecaEstoque p where p.id=:id")
    Optional<PecaEstoque> findByIdForUpdate(@Param("id") Long id);
}
