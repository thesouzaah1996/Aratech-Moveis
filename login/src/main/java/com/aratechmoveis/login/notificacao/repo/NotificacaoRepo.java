package com.aratechmoveis.login.notificacao.repo;

import com.aratechmoveis.login.notificacao.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepo extends JpaRepository<Notificacao, Long> {
}
