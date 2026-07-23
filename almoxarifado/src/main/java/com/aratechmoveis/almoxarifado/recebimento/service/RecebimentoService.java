package com.aratechmoveis.almoxarifado.recebimento.service;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.recebimento.dto.RecebimentoDTO;

public interface RecebimentoService {
    Response adicionarRecebimento(RecebimentoDTO recebimentoDTO);
    Response buscarFila();
    Response buscarHistorico();
    Response autorizarRecebimento(String notaFiscal);
    Response finalizarRecebimento(String notaFiscal);
    Response atualizarRecebimento(RecebimentoDTO recebimentoDTO);
}
