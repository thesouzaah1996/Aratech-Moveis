package com.aratechmoveis.almoxarifado.categoria.repository;

import com.aratechmoveis.almoxarifado.categoria.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Boolean existsByNomeIgnoreCase(String nome);
}
