package com.aratechmoveis.manutencao.entrega.repository;

import com.aratechmoveis.manutencao.entrega.entity.Entrega;
import com.aratechmoveis.manutencao.entrega.entity.StatusRecebimento;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface EntregaRepository {
    boolean existsByNotaFiscal(String notaFiscal);
    Optional<Entrega> findByNotaFiscal(String notaFiscal);
    List<Entrega> findByStatusRecebimento(StatusRecebimento statusRecebimento, Sort sort);
    List<Entrega> findByStatusRecebimentoNot(StatusRecebimento statusRecebimento, Sort sort);
}
