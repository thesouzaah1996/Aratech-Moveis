package com.aratechmoveis.login.notificacao.service;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.notificacao.dto.NotificacaoDTO;

public interface NotificacaoService {
    void enviarEmail(NotificacaoDTO notificacaoDTO, Funcionario funcionario);
}
