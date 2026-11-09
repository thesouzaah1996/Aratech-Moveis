package com.aratech.moveis.rh.perfis.repository;

import com.aratech.moveis.rh.perfis.model.Perfil;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNome(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Page<Perfil> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Perfil p where p.id=:id")
    Optional<Perfil> findByIdForUpdate(@Param("id") Long id);
}
