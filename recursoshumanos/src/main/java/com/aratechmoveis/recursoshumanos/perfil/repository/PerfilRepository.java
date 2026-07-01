package com.aratechmoveis.recursoshumanos.perfil.repository;

import com.aratechmoveis.recursoshumanos.perfil.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Boolean existsByNomeIgnoreCase(String nome);
}
