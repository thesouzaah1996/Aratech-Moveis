package com.aratechmoveis.manutencao.Mecanico.repository;

import com.aratechmoveis.manutencao.Mecanico.entity.Mecanico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MecanicoRepository extends JpaRepository<Mecanico, Long> {
    boolean existsByNomeIgnoreCase(String nome);
    List<Mecanico> findByAtivoTrue();
}
