package com.aratechmoveis.portaria.controle_acesso.repository;

import com.aratechmoveis.portaria.controle_acesso.entity.RegistroChegada;
import com.aratechmoveis.portaria.controle_acesso.entity.StatusCaminhao;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroChegadaRepository extends JpaRepository<RegistroChegada, Long> {
    boolean existsByNotaFiscal(String notaFiscal);
    List<RegistroChegada> findByStatus(StatusCaminhao status, Sort sort);
    List<RegistroChegada> findByStatusNot(StatusCaminhao status, Sort sort);
}
