package com.aratech.moveis.rh.funcionarios.validation;

import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.exception.CadastroFuncionarioException;
import com.aratech.moveis.rh.funcionarios.model.Funcionario;
import com.aratech.moveis.rh.funcionarios.repository.FuncionarioRepository;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class FuncionarioValidator {

    private final FuncionarioRepository funcionarioRepository;
    private final Validator validator;
    private final CpfValidator cpfValidator;

    public void validarCadastro(FuncionarioDTO dto) {
        if (dto == null) {
            throw new CadastroFuncionarioException(
                    HttpStatus.BAD_REQUEST, "Informe os dados do funcionário.");
        }
        var dados = new BeanWrapperImpl(dto);
        for (var propriedade : dados.getPropertyDescriptors()) {
            if (propriedade.getPropertyType() == String.class
                    && dados.isWritableProperty(propriedade.getName())) {
                String valor = (String) dados.getPropertyValue(propriedade.getName());
                if (valor != null) {
                    valor = valor.trim();
                    dados.setPropertyValue(propriedade.getName(), valor.isEmpty() ? null : valor);
                }
            }
        }
        if (dto.getEmail() != null) {
            dto.setEmail(dto.getEmail().toLowerCase(Locale.ROOT));
        }
        var erros = validator.validate(dto);
        if (!erros.isEmpty()) {
            throw new ConstraintViolationException(erros);
        }
        if (!cpfValidator.isValid(dto.getCpf())) {
            throw new CadastroFuncionarioException(
                    HttpStatus.BAD_REQUEST, "Informe um CPF válido.");
        }
        if (!dto.getDataAdmissao().isAfter(dto.getDataNascimento())) {
            throw new CadastroFuncionarioException(
                    HttpStatus.BAD_REQUEST,
                    "A data de admissão deve ser posterior à data de nascimento.");
        }
    }

    public void validarDuplicidade(String cpf, String email, String pisNitNis) {
        if (funcionarioRepository.existsByCpf(cpf)) {
            throw new CadastroFuncionarioException(
                    HttpStatus.CONFLICT, "Já existe um funcionário com este CPF.");
        }
        if (funcionarioRepository.existsByEmailIgnoreCase(email)) {
            throw new CadastroFuncionarioException(
                    HttpStatus.CONFLICT, "Este e-mail já está em uso.");
        }
        if (pisNitNis != null && funcionarioRepository.existsByPisNitNis(pisNitNis)) {
            throw new CadastroFuncionarioException(
                    HttpStatus.CONFLICT, "Este PIS/NIT/NIS já está cadastrado.");
        }
    }

    public void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new CadastroFuncionarioException(
                    HttpStatus.BAD_REQUEST, "Informe um ID positivo.");
        }
    }

    public void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0
                || tamanho < 1
                || tamanho > 100
                || (long) pagina * tamanho > Integer.MAX_VALUE) {
            throw new CadastroFuncionarioException(
                    HttpStatus.BAD_REQUEST,
                    "Pagina deve ser nao negativa e tamanho deve estar entre 1 e 100.");
        }
    }

    public void validarAtualizacao(FuncionarioDTO dto, Funcionario atual) {
        validarCadastro(dto);
        if (!atual.getCpf().equals(dto.getCpf())) {
            throw new CadastroFuncionarioException(
                    HttpStatus.CONFLICT, "O CPF do funcionario nao pode ser alterado.");
        }
        if (funcionarioRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), atual.getId())) {
            throw new CadastroFuncionarioException(
                    HttpStatus.CONFLICT, "Este e-mail ja esta em uso.");
        }
        if (dto.getPisNitNis() != null
                && funcionarioRepository.existsByPisNitNisAndIdNot(
                        dto.getPisNitNis(), atual.getId())) {
            throw new CadastroFuncionarioException(
                    HttpStatus.CONFLICT, "Este PIS/NIT/NIS ja esta cadastrado.");
        }
    }
}
