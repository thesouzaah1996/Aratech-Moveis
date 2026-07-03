package com.aratechmoveis.portaria.controle_acesso.service;

import com.aratechmoveis.portaria.response.Response;
import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;

public interface RegistroChegadaService {
    Response addRegistroChegada(RegistroChegadaDTO registroChegadaDTO);
    Response getFila();
    Response getHistorico();
    Response finalizarRegistro(Long id);
}
