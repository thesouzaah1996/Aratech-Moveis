package com.aratech.moveis.manutencao.chamados.repository;

import com.aratech.moveis.manutencao.chamados.enums.OrigemChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.model.Chamado;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ChamadoRepository
        extends JpaRepository<Chamado, Long>,
                org.springframework.data.jpa.repository.JpaSpecificationExecutor<Chamado> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Chamado c where c.id=:id")
    Optional<Chamado> findByIdForUpdate(
            @org.springframework.data.repository.query.Param("id") Long id);

    Optional<Chamado> findByChaveOperacao(UUID chave);

    Page<Chamado> findByOrigem(OrigemChamado origem, Pageable pageable);

    Page<Chamado> findByOrigemAndStatus(
            OrigemChamado origem, StatusChamado status, Pageable pageable);

    Page<Chamado> findByStatus(StatusChamado status, Pageable pageable);
}
