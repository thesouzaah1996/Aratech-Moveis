package com.aratechmoveis.login.dto;

import com.aratechmoveis.login.entity.Perfil;
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

    private Long id;

    private String nome;

    private String emailCorporativo;

    private List<Perfil> perfis;

    private boolean ativo;
}
