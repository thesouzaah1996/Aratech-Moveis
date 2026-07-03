package com.aratechmoveis.manutencao.pecaestoque.repository;

import com.aratechmoveis.manutencao.pecaestoque.entity.PecaEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PecaEstoqueRepository extends JpaRepository<PecaEstoque, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
