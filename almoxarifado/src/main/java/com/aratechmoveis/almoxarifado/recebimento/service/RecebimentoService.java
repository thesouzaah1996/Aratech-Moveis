package com.aratechmoveis.almoxarifado.recebimento.service;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.recebimento.dto.RecebimentoDTO;

public interface RecebimentoService {
    Response adicionarRecebimento(RecebimentoDTO recebimentoDTO);
    Response listarRecebimentos();
    Response buscarHistorico();
    Response autorizarRecebimento(String notaFiscal);
    Response finalizarRecebimento(String notaFiscal);
}
