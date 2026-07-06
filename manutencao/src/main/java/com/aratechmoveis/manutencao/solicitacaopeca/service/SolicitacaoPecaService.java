package com.aratechmoveis.manutencao.solicitacaopeca.service;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.solicitacaopeca.dto.SolicitacaoPecaDTO;

public interface SolicitacaoPecaService {
    Response adicionarSolicitacaoPeca(SolicitacaoPecaDTO solicitacaoPecaDTO);
    Response listarSolicitacoesPeca();
    Response buscarSolicitacaoPecaPorId(Long id);
    Response removerSolicitacaoPeca(Long id);
}
