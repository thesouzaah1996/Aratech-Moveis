package com.aratech.moveis.almoxarifado.produtos.validation;

import com.aratech.moveis.almoxarifado.produtos.exception.ProdutoException;

import jakarta.validation.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ProdutoValidator {
    private final Validator validator;

    public void validar(Object dto) {
        if (dto == null)
            throw new ProdutoException(HttpStatus.BAD_REQUEST, "Informe os dados da operacao.");
        var wrapper = new BeanWrapperImpl(dto);
        for (var propriedade : wrapper.getPropertyDescriptors()) {
            String campo = propriedade.getName();
            if (propriedade.getPropertyType() == String.class
                    && wrapper.isWritableProperty(campo)) {
                String valor = (String) wrapper.getPropertyValue(campo);
                if (valor == null) continue;
                if (valor.codePoints()
                        .anyMatch(
                                c ->
                                        (Character.isISOControl(c)
                                                        && !((campo.equals("descricao")
                                                                        || campo.equals("motivo"))
                                                                && (c == '\n' || c == '\r'
                                                                        || c == '\t')))
                                                || Character.getType(c) == Character.FORMAT)) {
                    throw new ProdutoException(
                            HttpStatus.BAD_REQUEST, "Os campos contem caracteres invalidos.");
                }
                valor = valor.strip();
                if (campo.equals("sku")) valor = valor.toUpperCase(Locale.ROOT);
                wrapper.setPropertyValue(campo, valor);
            }
        }
        var erros = validator.validate(dto);
        if (!erros.isEmpty()) throw new ConstraintViolationException(erros);
    }

    public void validarId(Long id) {
        if (id == null || id <= 0)
            throw new ProdutoException(HttpStatus.BAD_REQUEST, "Informe um ID positivo.");
    }

    public void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0
                || tamanho < 1
                || tamanho > 100
                || (long) pagina * tamanho > Integer.MAX_VALUE) {
            throw new ProdutoException(
                    HttpStatus.BAD_REQUEST,
                    "Pagina deve ser nao negativa e tamanho entre 1 e 100.");
        }
    }

    public String validarBusca(String busca) {
        if (busca == null) return "";
        if (busca.length() > 150
                || busca.codePoints()
                        .anyMatch(
                                c ->
                                        Character.isISOControl(c)
                                                || Character.getType(c) == Character.FORMAT)) {
            throw new ProdutoException(
                    HttpStatus.BAD_REQUEST, "Busca deve ter ate 150 caracteres validos.");
        }
        return busca.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
