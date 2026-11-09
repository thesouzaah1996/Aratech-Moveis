package com.aratech.moveis.portaria.controleacesso.repository;

import com.aratech.moveis.portaria.controleacesso.enums.StatusAcesso;
import com.aratech.moveis.portaria.controleacesso.model.RegistroChegada;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface RegistroChegadaRepository
        extends JpaRepository<RegistroChegada, Long>, JpaSpecificationExecutor<RegistroChegada> {
    Optional<RegistroChegada> findByChaveOperacao(UUID chave);

    boolean existsByNotaFiscalAndEmpresaIgnoreCaseAndIdNot(
            String notaFiscal, String empresa, Long id);

    boolean existsByPlacaAndStatusNotAndIdNot(String placa, StatusAcesso status, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RegistroChegada r where r.id=:id")
    Optional<RegistroChegada> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(StatusAcesso status);

    Optional<RegistroChegada> findFirstByOrderByIdDesc();

    long countBySetorResponsavelAndStatus(String setor, StatusAcesso status);

    Optional<RegistroChegada> findFirstBySetorResponsavelOrderByIdDesc(String setor);
}
