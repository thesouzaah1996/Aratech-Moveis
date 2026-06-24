package com.aratechmoveis.portaria.controle_acesso.service;

import com.aratechmoveis.portaria.Response;
import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;

public interface RegistroChegadaService {
    com.aratechmoveis.portaria.Response addRegistroChegada(RegistroChegadaDTO registroChegadaDTO);
    Response getRegistrosChegada();
}
