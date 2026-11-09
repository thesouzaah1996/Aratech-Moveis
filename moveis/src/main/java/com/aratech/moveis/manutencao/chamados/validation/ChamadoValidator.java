package com.aratech.moveis.manutencao.chamados.validation;

import com.aratech.moveis.manutencao.chamados.dto.ChamadoDTO;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.enums.TipoManutencao;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ChamadoValidator {
    public ChamadoDTO validar(ChamadoDTO dto) {
        if (dto == null) throw erro("Informe os dados da solicitacao.");
        String equipamento = texto(dto.equipamento(), "Equipamento", 2, 150, false);
        String solicitante = texto(dto.solicitante(), "Solicitante", 3, 150, false);
        String setor = texto(dto.setor(), "Setor", 2, 100, false);
        String descricao = texto(dto.descricao(), "Descricao", 3, 3000, true);
        valorEnum(TipoManutencao.class, dto.tipo(), "Tipo de manutencao invalido.");
        valorEnum(PrioridadeChamado.class, dto.prioridade(), "Prioridade invalida.");
        String telefone = telefone(dto.telefone());
        if (dto.chaveOperacao() == null)
            throw erro("Informe a chave da operacao para evitar solicitacoes duplicadas.");
        return new ChamadoDTO(
                equipamento,
                dto.tipo(),
                dto.prioridade(),
                solicitante,
                setor,
                telefone,
                descricao,
                dto.chaveOperacao());
    }

    public String telefone(String valor) {
        String telefone = valor == null ? "" : valor.strip();
        if (!telefone.isEmpty()
                && (!telefone.matches("[0-9() +\\-]{1,25}")
                        || !telefone.replaceAll("[^0-9]", "").matches("[1-9][0-9]{9,10}")))
            throw erro("Informe um telefone com DDD e 10 ou 11 digitos, ou deixe em branco.");
        telefone = telefone.replaceAll("[^0-9]", "");
        return telefone;
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
        String normalizado = multiline ? valor.strip() : valor.strip().replaceAll(" +", " ");
        if (normalizado.length() < min)
            throw erro(campo + ": preencha ao menos " + min + " caracteres.");
        return normalizado;
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

    public StatusChamado status(String status) {
        return status == null ? null : valorEnum(StatusChamado.class, status, "Status invalido.");
    }

    public <E extends Enum<E>> E valorEnum(Class<E> tipo, String valor, String mensagem) {
        try {
            return Enum.valueOf(tipo, valor == null ? "" : valor);
        } catch (IllegalArgumentException e) {
            throw erro(mensagem);
        }
    }

    private ChamadoException erro(String mensagem) {
        return new ChamadoException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
