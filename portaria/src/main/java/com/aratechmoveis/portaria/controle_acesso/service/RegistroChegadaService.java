package com.aratechmoveis.portaria.controle_acesso.service;

import com.aratechmoveis.portaria.response.Response;
import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;

public interface RegistroChegadaService {
    Response adicionarRegistroChegada(RegistroChegadaDTO registroChegadaDTO);
    Response buscarFila();
    Response buscarHistorico();
    Response finalizarRegistro(Long id);
}
