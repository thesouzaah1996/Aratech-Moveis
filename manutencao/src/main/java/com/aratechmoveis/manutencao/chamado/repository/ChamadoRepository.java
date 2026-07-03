package com.aratechmoveis.manutencao.chamado.repository;

import com.aratechmoveis.manutencao.chamado.entity.Chamado;
import com.aratechmoveis.manutencao.chamado.entity.StatusChamado;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    List<Chamado> findByStatus(StatusChamado status, Sort sort);
}
