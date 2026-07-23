package com.aratechmoveis.portaria.controle_acesso.service;

import com.aratechmoveis.portaria.controle_acesso.entity.StatusCaminhao;
import com.aratechmoveis.portaria.response.Response;
import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;

public interface RegistroChegadaService {
    Response adicionarRegistroChegada(RegistroChegadaDTO registroChegadaDTO);
    Response buscarFila();
    Response buscarHistorico();
    Response listarAutorizados(StatusCaminhao statusCaminhao);
    Response autorizarRecebimento(String notaFiscal);
    Response finalizarRegistro(String notaFiscal);
    Response atualizarRegistroChegada(RegistroChegadaDTO registroChegadaDTO);
}
