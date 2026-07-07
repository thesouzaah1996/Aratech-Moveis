package com.aratechmoveis.login.funcionario.mapper;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.subscriber.representation.FuncionarioRepresentation;
import org.springframework.stereotype.Component;


@Component
public class FuncionarioMapper {

    public Funcionario map(FuncionarioRepresentation funcionarioRepresentation) {

        Funcionario novoFuncionario = new Funcionario();
        novoFuncionario.setIdFuncionario(funcionarioRepresentation.idFuncionario());
        novoFuncionario.setNomeFuncionario(funcionarioRepresentation.nomeFuncionario());
        novoFuncionario.setEmailPessoal(funcionarioRepresentation.emailPessoal());
        novoFuncionario.setEmailCorporativo(funcionarioRepresentation.emailCorporativo());
        novoFuncionario.setPerfis(funcionarioRepresentation.perfis());
        novoFuncionario.setAtivo(funcionarioRepresentation.ativo());

        return novoFuncionario;
    }
}
