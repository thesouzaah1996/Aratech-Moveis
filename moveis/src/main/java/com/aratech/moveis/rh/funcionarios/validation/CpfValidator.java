package com.aratech.moveis.rh.funcionarios.validation;

import org.springframework.stereotype.Component;

@Component
public class CpfValidator {

    public boolean isValid(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }
        if (cpf.chars().allMatch(digito -> digito == cpf.charAt(0))) {
            return false;
        }
        for (int posicao = 9; posicao <= 10; posicao++) {
            int soma = 0;
            for (int indice = 0; indice < posicao; indice++) {
                soma += (cpf.charAt(indice) - '0') * (posicao + 1 - indice);
            }
            int resto = soma % 11;
            int digito = resto < 2 ? 0 : 11 - resto;
            if (cpf.charAt(posicao) - '0' != digito) {
                return false;
            }
        }
        return true;
    }
}
