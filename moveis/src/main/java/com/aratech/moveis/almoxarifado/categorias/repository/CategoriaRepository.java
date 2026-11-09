package com.aratech.moveis.almoxarifado.categorias.repository;

import com.aratech.moveis.almoxarifado.categorias.model.Categoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Page<Categoria> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Categoria> findByNomeContainingIgnoreCaseAndAtivo(
            String nome, Boolean ativo, Pageable pageable);

    Page<Categoria> findByAtivoTrue(Pageable pageable);

    Page<Categoria> findByAtivoFalse(Pageable pageable);

    Optional<Categoria> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Categoria c where c.id = :id")
    Optional<Categoria> findByIdForUpdate(
            @org.springframework.data.repository.query.Param("id") Long id);
}
