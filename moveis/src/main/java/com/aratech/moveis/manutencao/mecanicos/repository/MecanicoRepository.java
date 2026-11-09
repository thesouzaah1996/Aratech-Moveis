package com.aratech.moveis.manutencao.mecanicos.repository;

import com.aratech.moveis.manutencao.mecanicos.model.Mecanico;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MecanicoRepository extends JpaRepository<Mecanico, Long> {
    Page<Mecanico> findByAtivo(boolean ativo, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mecanico m where m.id=:id")
    Optional<Mecanico> findByIdForUpdate(@Param("id") Long id);
}
