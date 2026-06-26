package com.aratechmoveis.portaria;

import com.aratechmoveis.portaria.controle_acesso.dto.RegistroChegadaDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private int status;
    private String mensagem;

    private RegistroChegadaDTO registroChegada;
    private List<RegistroChegadaDTO> registrosChegada;
}
