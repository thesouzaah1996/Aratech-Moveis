package com.aratechmoveis.recursoshumanos.response;

import com.aratechmoveis.recursoshumanos.funcionarios.dto.FuncionarioDTO;
import com.aratechmoveis.recursoshumanos.perfil.dto.PerfilDTO;
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

    private FuncionarioDTO funcionario;
    private List<FuncionarioDTO> funcionarios;

    private PerfilDTO perfil;
    private List<PerfilDTO> perfis;
}
