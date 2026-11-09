package com.aratech.moveis.assistencia.lotes.repository;

import com.aratech.moveis.assistencia.lotes.model.LoteAssistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoteAssistenciaRepository extends JpaRepository<LoteAssistencia, Long> {
    List<LoteAssistencia> findAllByOrderByDataEntradaAscIdAsc();

    Optional<LoteAssistencia> findByOrigemId(Long origemId);
}
