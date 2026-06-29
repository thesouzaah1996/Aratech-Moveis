package com.aratechmoveis.login.perfil.repository;

import com.aratechmoveis.login.perfil.modelo.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Boolean existsByNomeIgnoreCase(String nome);
}
