package com.aratech.moveis.manutencao.estoque.repository;

import com.aratech.moveis.manutencao.estoque.model.MovimentacaoPeca;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface MovimentacaoPecaRepository extends JpaRepository<MovimentacaoPeca, Long> {
    Optional<MovimentacaoPeca> findByChaveOperacao(UUID chave);

    Page<MovimentacaoPeca> findByPecaId(Long pecaId, Pageable pageable);
}
