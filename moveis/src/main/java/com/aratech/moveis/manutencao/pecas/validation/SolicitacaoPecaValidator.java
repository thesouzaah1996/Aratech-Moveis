package com.aratech.moveis.manutencao.pecas.validation;

import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaDTO;
import com.aratech.moveis.manutencao.pecas.enums.FinalidadePeca;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SolicitacaoPecaValidator {
    private final ChamadoValidator comum;

    public SolicitacaoPecaDTO validar(SolicitacaoPecaDTO dto) {
        if (dto == null) throw erro("Informe os dados da solicitacao de peca.");
        String nome = comum.texto(dto.nomePeca(), "Nome da peca", 2, 150, false);
        String codigo =
                comum.texto(dto.codigo() == null ? "" : dto.codigo(), "Codigo", 0, 100, false);
        var quantidade = dto.quantidade();
        if (quantidade == null
                || quantidade.signum() <= 0
                || quantidade.compareTo(new BigDecimal("999999999999.999")) > 0
                || quantidade.stripTrailingZeros().scale() > 3)
            throw erro(
                    "Informe uma quantidade positiva com ate 3 casas decimais e no maximo"
                            + " 999999999999.999.");
        if (!Set.of("un", "kg", "L", "m", "cx", "par")
                .contains(dto.unidade() == null ? "" : dto.unidade()))
            throw erro("Unidade invalida.");
        comum.valorEnum(FinalidadePeca.class, dto.finalidade(), "Finalidade invalida.");
        comum.valorEnum(PrioridadeChamado.class, dto.prioridade(), "Prioridade invalida.");
        if (dto.chaveOperacao() == null)
            throw erro("Informe a chave da operacao para evitar solicitacoes duplicadas.");
        return new SolicitacaoPecaDTO(
                nome,
                codigo,
                quantidade,
                dto.unidade(),
                comum.texto(dto.equipamento(), "Equipamento", 2, 150, false),
                dto.finalidade(),
                dto.prioridade(),
                comum.texto(dto.solicitante(), "Solicitante", 3, 150, false),
                comum.texto(dto.setor(), "Setor", 2, 100, false),
                comum.telefone(dto.telefone()),
                comum.texto(dto.observacoes(), "Observacoes", 3, 3000, true),
                dto.chaveOperacao());
    }

    private ChamadoException erro(String mensagem) {
        return new ChamadoException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
