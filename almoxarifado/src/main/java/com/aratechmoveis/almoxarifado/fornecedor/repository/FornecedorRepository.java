package com.aratechmoveis.almoxarifado.fornecedor.repository;

import com.aratechmoveis.almoxarifado.fornecedor.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    boolean existsByEmail(String email);
}
