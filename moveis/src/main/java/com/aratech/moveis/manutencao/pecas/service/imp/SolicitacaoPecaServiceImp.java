package com.aratech.moveis.manutencao.pecas.service.imp;

import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;
import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.chamados.enums.OrigemChamado;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.chamados.repository.ChamadoRepository;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaDTO;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaResumoDTO;
import com.aratech.moveis.manutencao.pecas.enums.FinalidadePeca;
import com.aratech.moveis.manutencao.pecas.service.SolicitacaoPecaService;
import com.aratech.moveis.manutencao.pecas.validation.SolicitacaoPecaValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SolicitacaoPecaServiceImp implements SolicitacaoPecaService {
    private final ChamadoRepository repository;
    private final SolicitacaoPecaValidator validator;
    private final ChamadoValidator comum;
    private final PedidoCompraService compras;

    @Override
    @Transactional
    public Response solicitar(SolicitacaoPecaDTO dto) {
        var d = validator.validar(dto);
        var anterior = repository.findByChaveOperacao(d.chaveOperacao());
        if (anterior.isPresent()) {
            if (!mesmosDados(anterior.get(), d))
                throw new ChamadoException(
                        HttpStatus.CONFLICT, "Chave da operacao ja utilizada com outros dados.");
            compras.solicitacao(anterior.get());
            return resposta(anterior.get(), 201);
        }
        var c =
                Chamado.builder()
                        .origem(OrigemChamado.PECA)
                        .nomePeca(d.nomePeca())
                        .codigoPeca(d.codigo())
                        .quantidade(d.quantidade())
                        .unidade(d.unidade())
                        .equipamento(d.equipamento())
                        .finalidade(FinalidadePeca.valueOf(d.finalidade()))
                        .prioridade(PrioridadeChamado.valueOf(d.prioridade()))
                        .solicitante(d.solicitante())
                        .setor(d.setor())
                        .telefone(d.telefone())
                        .descricao(d.observacoes())
                        .chaveOperacao(d.chaveOperacao())
                        .dataAbertura(Instant.now().truncatedTo(ChronoUnit.MICROS))
                        .build();
        var salvo = repository.saveAndFlush(c);
        compras.solicitacao(salvo);
        return resposta(salvo, 201);
    }

    @Override
    @Transactional(readOnly = true)
    public Response buscar(Long id) {
        comum.id(id);
        var c =
                repository
                        .findById(id)
                        .filter(r -> r.getOrigem() == OrigemChamado.PECA)
                        .orElseThrow(
                                () ->
                                        new ChamadoException(
                                                HttpStatus.NOT_FOUND,
                                                "Solicitacao de peca nao encontrada."));
        return resposta(c, 200);
    }

    private boolean mesmosDados(Chamado c, SolicitacaoPecaDTO d) {
        return c.getOrigem() == OrigemChamado.PECA
                && Objects.equals(c.getNomePeca(), d.nomePeca())
                && Objects.equals(c.getCodigoPeca(), d.codigo())
                && c.getQuantidade().compareTo(d.quantidade()) == 0
                && Objects.equals(c.getUnidade(), d.unidade())
                && Objects.equals(c.getEquipamento(), d.equipamento())
                && c.getFinalidade().name().equals(d.finalidade())
                && c.getPrioridade().name().equals(d.prioridade())
                && Objects.equals(c.getSolicitante(), d.solicitante())
                && Objects.equals(c.getSetor(), d.setor())
                && Objects.equals(c.getTelefone(), d.telefone())
                && Objects.equals(c.getDescricao(), d.observacoes());
    }

    private Response resposta(Chamado c, int status) {
        return Response.builder()
                .status(status)
                .mensagem("Solicitacao de peca registrada.")
                .solicitacao(SolicitacaoPecaResumoDTO.from(c))
                .build();
    }
}
