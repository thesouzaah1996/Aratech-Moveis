package com.aratechmoveis.login.funcionario.subscriber.representation;

import com.aratechmoveis.login.funcionario.entity.Perfil;

import java.util.List;

public record FuncionarioRepresentation(
        Long idFuncionario,
        String nomeFuncionario,
        String emailPessoal,
        String emailCorporativo,
        List<Perfil> perfis,
        boolean ativo
) {
}
