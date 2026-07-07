package com.aratechmoveis.login.funcionario.dto;

import com.aratechmoveis.login.funcionario.entity.Perfil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FuncionarioDTO {

    private Long idFuncionario;

    private String nomeFuncionario;

    private String emailCorporativo;

    private List<Perfil> perfis;

    private boolean ativo;
}
