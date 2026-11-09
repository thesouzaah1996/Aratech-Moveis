package com.aratech.moveis.suporte.chamados.repository;

import com.aratech.moveis.suporte.chamados.model.ChamadoTi;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface ChamadoTiRepository
        extends JpaRepository<ChamadoTi, Long>, JpaSpecificationExecutor<ChamadoTi> {
    Optional<ChamadoTi> findByChaveOperacao(UUID chave);
}
