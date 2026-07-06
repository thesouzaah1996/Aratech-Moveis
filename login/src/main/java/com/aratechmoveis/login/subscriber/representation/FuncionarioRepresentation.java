package com.aratechmoveis.login.subscriber.representation;

import com.aratechmoveis.login.entity.Perfil;

import java.util.List;

public record FuncionarioRepresentation(
        Long idFuncionario,
        String nomeFuncionario,
        String emailCorporativo,
        List<Perfil> perfis,
        boolean ativo
) {
}
