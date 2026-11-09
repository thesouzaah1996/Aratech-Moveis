package com.aratech.moveis.almoxarifado.fornecedores.validation;

import com.aratech.moveis.almoxarifado.fornecedores.dto.FornecedorDTO;
import com.aratech.moveis.almoxarifado.fornecedores.exception.FornecedorException;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class FornecedorValidator {
    private final Validator validator;

    public void validarDados(FornecedorDTO dto) {
        if (dto == null)
            throw new FornecedorException(
                    HttpStatus.BAD_REQUEST, "Informe os dados do fornecedor.");
        dto.setNome(texto(dto.getNome()));
        dto.setEmail(email(dto.getEmail()));
        dto.setTelefone(texto(dto.getTelefone()));
        if (dto.getRepresentante() != null) {
            var rep = dto.getRepresentante();
            rep.setNomeRepresentante(texto(rep.getNomeRepresentante()));
            rep.setTelefoneRepresentante(texto(rep.getTelefoneRepresentante()));
            rep.setEmailRepresentante(email(rep.getEmailRepresentante()));
        }
        var erros = validator.validate(dto);
        if (!erros.isEmpty()) throw new ConstraintViolationException(erros);
    }

    private String texto(String valor) {
        if (valor == null) return null;
        if (valor.codePoints()
                .anyMatch(
                        c ->
                                Character.isISOControl(c)
                                        || Character.getType(c) == Character.FORMAT)) {
            throw new FornecedorException(
                    HttpStatus.BAD_REQUEST, "Os campos contem caracteres invalidos.");
        }
        return valor.strip().replaceAll(" +", " ");
    }

    private String email(String valor) {
        String normalizado = texto(valor);
        return normalizado == null ? null : normalizado.toLowerCase(Locale.ROOT);
    }

    public void validarId(Long id) {
        if (id == null || id <= 0)
            throw new FornecedorException(HttpStatus.BAD_REQUEST, "Informe um ID positivo.");
    }

    public void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0
                || tamanho < 1
                || tamanho > 100
                || (long) pagina * tamanho > Integer.MAX_VALUE) {
            throw new FornecedorException(
                    HttpStatus.BAD_REQUEST,
                    "Pagina deve ser nao negativa e tamanho entre 1 e 100.");
        }
    }

    public String validarBusca(String busca) {
        String texto = texto(busca);
        if (texto == null) return "";
        if (texto.length() > 150)
            throw new FornecedorException(
                    HttpStatus.BAD_REQUEST, "Busca deve ter ate 150 caracteres.");
        return texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
