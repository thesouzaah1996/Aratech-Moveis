package com.aratechmoveis.almoxarifado.recebimento.repository;

import com.aratechmoveis.almoxarifado.recebimento.entity.Recebimento;
import com.aratechmoveis.almoxarifado.recebimento.entity.StatusRecebimento;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecebimentoRepository extends JpaRepository<Recebimento, Long> {
    boolean existsByNotaFiscal(String notaFiscal);
    Optional<Recebimento> findByNotaFiscal(String notaFiscal);
    List<Recebimento> findByStatusRecebimento(StatusRecebimento statusRecebimento, Sort sort);
    List<Recebimento> findByStatusRecebimentoNot(StatusRecebimento statusRecebimento, Sort sort);
}
