package com.aratech.moveis.suporte.chamados.validation;

import com.aratech.moveis.suporte.chamados.dto.ChamadoTiDTO;
import com.aratech.moveis.suporte.chamados.enums.PrioridadeTi;
import com.aratech.moveis.suporte.chamados.enums.StatusChamadoTi;
import com.aratech.moveis.suporte.chamados.enums.TipoChamadoTi;
import com.aratech.moveis.suporte.chamados.exception.ChamadoTiException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ChamadoTiValidator {
    private static final Set<String> MODULOS =
            Set.of(
                    "",
                    "Login / Autenticação",
                    "Dashboard",
                    "Almoxarifado",
                    "Portaria",
                    "Carregamento",
                    "Manutenção",
                    "Assistência Técnica",
                    "Recursos Humanos",
                    "PCP",
                    "Compras",
                    "Financeiro",
                    "Administração");

    public ChamadoTiDTO validar(ChamadoTiDTO d) {
        if (d == null) throw erro("Informe os dados do chamado.");
        rotulo(TipoChamadoTi.class, d.tipo(), "Selecione um tipo de problema valido.");
        rotulo(PrioridadeTi.class, d.prioridade(), "Selecione uma prioridade valida.");
        String modulo = d.moduloAfetado() == null ? "" : d.moduloAfetado();
        if (!MODULOS.contains(modulo)) throw erro("Selecione um modulo valido ou deixe em branco.");
        String contato = d.contato() == null ? "" : d.contato().strip();
        if (!contato.isEmpty()
                && (!contato.matches("[0-9() +\\-]{1,25}")
                        || !contato.replaceAll("[^0-9]", "")
                                .matches("(?:[0-9]{2,6}|[1-9][0-9]{9,10})")))
            throw erro("Informe um ramal de 2 a 6 digitos ou telefone com DDD (10 ou 11 digitos).");
        if (d.chaveOperacao() == null)
            throw erro("Informe a chave da operacao para evitar chamados duplicados.");
        return new ChamadoTiDTO(
                d.tipo(),
                d.prioridade(),
                modulo,
                texto(d.titulo(), "Titulo", 3, 150, false),
                texto(d.solicitante(), "Solicitante", 3, 150, false),
                texto(d.setor(), "Setor", 2, 100, false),
                contato.replaceAll("[^0-9]", ""),
                texto(d.descricao(), "Descricao", 3, 3000, true),
                texto(
                        d.tentativasSolucao() == null ? "" : d.tentativasSolucao(),
                        "Tentativas de solucao",
                        0,
                        2000,
                        true),
                d.chaveOperacao());
    }

    public String texto(String valor, String campo, int min, int max, boolean multiline) {
        if (valor == null
                || valor.length() > max
                || valor.codePoints()
                        .anyMatch(
                                c ->
                                        (Character.isISOControl(c)
                                                        && !(multiline
                                                                && (c == 10 || c == 13 || c == 9)))
                                                || Character.getType(c) == Character.FORMAT))
            throw erro(campo + ": confira o tamanho e os caracteres informados.");
        String s = multiline ? valor.strip() : valor.strip().replaceAll(" +", " ");
        if (s.length() < min) throw erro(campo + ": preencha ao menos " + min + " caracteres.");
        return s;
    }

    public <E extends Enum<E>> E rotulo(Class<E> tipo, String valor, String mensagem) {
        return Arrays.stream(tipo.getEnumConstants())
                .filter(e -> e.toString().equals(valor))
                .findFirst()
                .orElseThrow(() -> erro(mensagem));
    }

    public StatusChamadoTi status(String status) {
        return status == null ? null : rotulo(StatusChamadoTi.class, status, "Status invalido.");
    }

    public void id(Long id) {
        if (id == null || id <= 0) throw erro("Informe um ID positivo.");
    }

    public void paginacao(int pagina, int tamanho) {
        if (pagina < 0
                || tamanho < 1
                || tamanho > 100
                || (long) pagina * tamanho > Integer.MAX_VALUE)
            throw erro("Pagina deve ser nao negativa e tamanho deve estar entre 1 e 100.");
    }

    private ChamadoTiException erro(String mensagem) {
        return new ChamadoTiException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
