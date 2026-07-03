package com.aratechmoveis.manutencao.solicitacaopeca.repository;

import com.aratechmoveis.manutencao.solicitacaopeca.entity.SolicitacaoPeca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoPecaRepository extends JpaRepository<SolicitacaoPeca, Long> {
}
