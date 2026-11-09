package com.aratech.moveis.manutencao.pecas.service;

import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaDTO;

public interface SolicitacaoPecaService {
    Response solicitar(SolicitacaoPecaDTO dto);

    Response buscar(Long id);
}
