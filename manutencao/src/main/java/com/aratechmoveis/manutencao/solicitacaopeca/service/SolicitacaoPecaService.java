package com.aratechmoveis.manutencao.solicitacaopeca.service;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.solicitacaopeca.dto.SolicitacaoPecaDTO;

public interface SolicitacaoPecaService {
    Response addSolicitacaoPeca(SolicitacaoPecaDTO solicitacaoPecaDTO);
    Response getSolicitacoesPeca();
    Response getSolicitacaoPecaById(Long id);
    Response deleteSolicitacaoPeca(Long id);
}
