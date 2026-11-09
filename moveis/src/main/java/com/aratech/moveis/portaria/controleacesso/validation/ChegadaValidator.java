package com.aratech.moveis.portaria.controleacesso.validation;

import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.exception.ControleAcessoException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class ChegadaValidator {
    public ChegadaDTO validar(ChegadaDTO dto, boolean cadastro) {
        if (dto == null) throw erro("Informe os dados da chegada.");
        String nf = texto(dto.notaFiscal(), "Nota fiscal", 1, 44);
        if (!notaValida(nf))
            throw erro(
                    "Informe o numero da nota fiscal (1 a 9 digitos, diferente de zero) ou uma"
                            + " chave de acesso de 44 digitos com digito verificador valido.");
        String empresa = texto(dto.empresa(), "Empresa", 2, 150);
        String motorista = texto(dto.nomeMotorista(), "Motorista", 3, 150);
        String placa = texto(dto.placa(), "Placa", 7, 8).toUpperCase(Locale.ROOT);
        if (!placa.matches("[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}"))
            throw erro("Informe uma placa brasileira valida, tradicional ou Mercosul.");
        placa = placa.replace("-", "");
        String carga = texto(dto.descricaoCarga(), "Descricao da carga", 3, 1000);
        if (!Set.of("ALMOXARIFADO", "CARREGAMENTO")
                .contains(dto.setorResponsavel() == null ? "" : dto.setorResponsavel()))
            throw erro("Selecione almoxarifado ou carregamento.");
        String operacao = null;
        if ("CARREGAMENTO".equals(dto.setorResponsavel())) {
            operacao = dto.tipoOperacao();
            if (!"CARREGAR".equals(operacao) && !"DESCARREGAR".equals(operacao))
                throw erro("Selecione carregar ou descarregar.");
        }
        if (cadastro && dto.chaveOperacao() == null)
            throw erro("Informe a chave da operacao para evitar chegadas duplicadas.");
        return new ChegadaDTO(
                nf,
                empresa,
                motorista,
                placa,
                carga,
                dto.setorResponsavel(),
                dto.chaveOperacao(),
                operacao);
    }

    private boolean notaValida(String nf) {
        if (nf.matches("[0-9]{1,9}")) return !nf.matches("0+");
        if (!nf.matches("[0-9]{44}") || nf.matches("([0-9])\\1{43}")) return false;
        int soma = 0, peso = 2;
        for (int i = 42; i >= 0; i--) {
            soma += (nf.charAt(i) - '0') * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int digito = 11 - soma % 11;
        if (digito >= 10) digito = 0;
        return nf.charAt(43) - '0' == digito;
    }

    private String texto(String valor, String campo, int min, int max) {
        if (valor == null
                || valor.length() > max
                || valor.codePoints()
                        .anyMatch(
                                c ->
                                        Character.isISOControl(c)
                                                || Character.getType(c) == Character.FORMAT))
            throw erro(campo + ": confira o tamanho e os caracteres informados.");
        String texto = valor.strip().replaceAll(" +", " ");
        if (texto.length() < min) throw erro(campo + ": preencha ao menos " + min + " caracteres.");
        return texto;
    }

    public void id(Long id) {
        if (id == null || id <= 0) throw erro("Informe um ID positivo.");
    }

    public void paginacao(int pagina, int tamanho) {
        if (pagina < 0
                || tamanho < 1
                || tamanho > 100
                || (long) pagina * tamanho > Integer.MAX_VALUE) throw erro("Paginacao invalida.");
    }

    public String notaFiltro(String nota) {
        if (nota == null || nota.isBlank()) return "";
        if (!nota.matches("[0-9]{1,44}")) throw erro("Busca por nota fiscal invalida.");
        return nota;
    }

    private ControleAcessoException erro(String mensagem) {
        return new ControleAcessoException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
