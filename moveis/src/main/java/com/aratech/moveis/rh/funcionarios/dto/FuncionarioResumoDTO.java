package com.aratech.moveis.rh.funcionarios.dto;

import com.aratech.moveis.rh.funcionarios.model.Funcionario;

/** Dados de consulta: nao expor documentos pessoais, salario ou conta bancaria. */
public record FuncionarioResumoDTO(
        Long id,
        String nomeCompleto,
        String email,
        String cargo,
        String setor,
        String telefoneFixo,
        Boolean ativo) {
    public static FuncionarioResumoDTO from(Funcionario funcionario) {
        return new FuncionarioResumoDTO(
                funcionario.getId(),
                funcionario.getNomeCompleto(),
                funcionario.getEmail(),
                funcionario.getCargo(),
                funcionario.getSetor(),
                funcionario.getTelefoneFixo(),
                funcionario.getAtivo());
    }
}
