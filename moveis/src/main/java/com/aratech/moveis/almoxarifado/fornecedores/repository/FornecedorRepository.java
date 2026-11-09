package com.aratech.moveis.almoxarifado.fornecedores.repository;

import com.aratech.moveis.almoxarifado.fornecedores.model.Fornecedor;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query(
            "select f from Fornecedor f where (:busca = '' or lower(f.nome) like lower(concat('%',"
                + " :busca, '%')) escape '!' or cast(f.id as string) = :busca) and (:ativo is null"
                + " or f.ativo = :ativo)")
    Page<Fornecedor> buscar(
            @Param("busca") String busca, @Param("ativo") Boolean ativo, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Fornecedor f where f.id = :id")
    Optional<Fornecedor> findByIdForUpdate(@Param("id") Long id);
}
