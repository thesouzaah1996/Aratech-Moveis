package com.aratechmoveis.login.mapper;

import com.aratechmoveis.login.dto.FuncionarioDTO;
import com.aratechmoveis.login.subscriber.representation.FuncionarioRepresentation;
import org.springframework.stereotype.Component;


@Component
public class FuncionarioMapper {

    public FuncionarioDTO map(FuncionarioRepresentation funcionarioRepresentation) {

        FuncionarioDTO funcionario = new FuncionarioDTO(
                funcionarioRepresentation.idFuncionario(),
                funcionarioRepresentation.nomeFuncionario(),
                funcionarioRepresentation.emailCorporativo(),
                funcionarioRepresentation.perfis(),
                funcionarioRepresentation.ativo()
        );

        return new FuncionarioDTO(
                funcionarioRepresentation.idFuncionario(),
                funcionarioRepresentation.nomeFuncionario(),
                funcionarioRepresentation.emailCorporativo(),
                funcionarioRepresentation.perfis(),
                funcionarioRepresentation.ativo()
        );
    }
}
