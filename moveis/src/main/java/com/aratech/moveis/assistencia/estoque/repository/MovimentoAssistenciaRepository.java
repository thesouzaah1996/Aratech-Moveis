package com.aratech.moveis.assistencia.estoque.repository;

import com.aratech.moveis.assistencia.estoque.model.MovimentoAssistencia;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface MovimentoAssistenciaRepository extends JpaRepository<MovimentoAssistencia, Long> {
    Optional<MovimentoAssistencia> findByChaveOperacao(UUID chave);

    Page<MovimentoAssistencia> findByPecaId(Long pecaId, Pageable pageable);
}
