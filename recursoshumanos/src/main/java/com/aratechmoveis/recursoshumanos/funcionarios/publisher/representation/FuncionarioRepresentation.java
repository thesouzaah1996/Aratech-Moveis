package com.aratechmoveis.recursoshumanos.funcionarios.publisher.representation;

import com.aratechmoveis.recursoshumanos.perfil.entity.Perfil;

import java.util.List;

public record FuncionarioRepresentation(
        Long idFuncionario,
        String nomeFuncionario,
        String emailCorporativo,
        List<Perfil> perfis,
        Boolean ativo
) {
}
