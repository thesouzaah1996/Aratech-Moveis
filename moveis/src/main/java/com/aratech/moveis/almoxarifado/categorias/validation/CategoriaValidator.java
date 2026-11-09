package com.aratech.moveis.almoxarifado.categorias.validation;

import com.aratech.moveis.almoxarifado.categorias.dto.CategoriaDTO;
import com.aratech.moveis.almoxarifado.categorias.exception.CategoriaException;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoriaValidator {
    private final Validator validator;

    public String validarNome(CategoriaDTO dto) {
        if (dto == null) {
            throw new CategoriaException(HttpStatus.BAD_REQUEST, "Informe os dados da categoria.");
        }
        String nome = dto.getNome();
        if (nome != null) {
            if (nome.codePoints()
                    .anyMatch(
                            c ->
                                    Character.isISOControl(c)
                                            || Character.getType(c) == Character.FORMAT)) {
                throw new CategoriaException(
                        HttpStatus.BAD_REQUEST, "O nome contem caracteres invalidos.");
            }
            dto.setNome(nome.strip().replaceAll(" +", " "));
        }
        var erros = validator.validate(dto);
        if (!erros.isEmpty()) throw new ConstraintViolationException(erros);
        return dto.getNome();
    }

    public void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new CategoriaException(HttpStatus.BAD_REQUEST, "Informe um ID positivo.");
        }
    }

    public void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0
                || tamanho < 1
                || tamanho > 100
                || (long) pagina * tamanho > Integer.MAX_VALUE) {
            throw new CategoriaException(
                    HttpStatus.BAD_REQUEST,
                    "Pagina deve ser nao negativa e tamanho deve estar entre 1 e 100.");
        }
    }
}
