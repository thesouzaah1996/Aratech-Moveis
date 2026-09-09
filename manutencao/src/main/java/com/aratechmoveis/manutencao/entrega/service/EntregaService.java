package com.aratechmoveis.manutencao.entrega.service;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.entrega.dto.EntregaDTO;

public interface EntregaService {
    Response adicionarEntrega(EntregaDTO entregaDTO);
    Response buscarFila();
    Response buscarHistorico();
    Response autorizarEntrega(String notaFiscal);
    Response finalizarEntrega(String notaFiscal);
    Response atualizarEntrega(EntregaDTO entregaDTO);
}
