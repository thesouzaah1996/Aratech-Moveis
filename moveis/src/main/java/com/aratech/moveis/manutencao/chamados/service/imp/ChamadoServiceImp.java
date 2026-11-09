package com.aratech.moveis.manutencao.chamados.service.imp;

import com.aratech.moveis.manutencao.chamados.dto.ChamadoDTO;
import com.aratech.moveis.manutencao.chamados.dto.ChamadoResumoDTO;
import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.chamados.enums.OrigemChamado;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.TipoManutencao;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.chamados.repository.ChamadoRepository;
import com.aratech.moveis.manutencao.chamados.service.ChamadoService;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChamadoServiceImp implements ChamadoService {
    private final ChamadoRepository repository;
    private final ChamadoValidator validator;

    @Override
    @Transactional
    public Response solicitar(ChamadoDTO dto) {
        var dados = validator.validar(dto);
        var anterior = repository.findByChaveOperacao(dados.chaveOperacao());
        if (anterior.isPresent()) {
            if (!mesmosDados(anterior.get(), dados))
                throw new ChamadoException(
                        HttpStatus.CONFLICT, "Chave da operacao ja utilizada com outros dados.");
            return resposta(anterior.get(), HttpStatus.CREATED, "Solicitacao ja registrada.");
        }
        var chamado =
                Chamado.builder()
                        .equipamento(dados.equipamento())
                        .tipo(TipoManutencao.valueOf(dados.tipo()))
                        .prioridade(PrioridadeChamado.valueOf(dados.prioridade()))
                        .solicitante(dados.solicitante())
                        .setor(dados.setor())
                        .telefone(dados.telefone())
                        .descricao(dados.descricao())
                        .chaveOperacao(dados.chaveOperacao())
                        .dataAbertura(Instant.now().truncatedTo(ChronoUnit.MICROS))
                        .build();
        return resposta(
                repository.saveAndFlush(chamado),
                HttpStatus.CREATED,
                "Solicitacao de manutencao registrada com sucesso.");
    }

    @Override
    @Transactional(readOnly = true)
    public Response listar(int pagina, int tamanho, String status) {
        validator.paginacao(pagina, tamanho);
        var filtro = validator.status(status);
        var pageable =
                PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataAbertura", "id"));
        var resultado =
                filtro == null
                        ? repository.findByOrigem(OrigemChamado.MANUTENCAO, pageable)
                        : repository.findByOrigemAndStatus(
                                OrigemChamado.MANUTENCAO, filtro, pageable);
        return Response.builder()
                .status(200)
                .chamados(resultado.getContent().stream().map(ChamadoResumoDTO::from).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(resultado.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response buscar(Long id) {
        validator.id(id);
        var chamado =
                repository
                        .findById(id)
                        .filter(c -> c.getOrigem() == OrigemChamado.MANUTENCAO)
                        .orElseThrow(
                                () ->
                                        new ChamadoException(
                                                HttpStatus.NOT_FOUND,
                                                "Solicitacao de manutencao nao encontrada."));
        return resposta(chamado, HttpStatus.OK, "Solicitacao encontrada.");
    }

    private boolean mesmosDados(Chamado c, ChamadoDTO d) {
        return c.getOrigem() == OrigemChamado.MANUTENCAO
                && Objects.equals(c.getEquipamento(), d.equipamento())
                && c.getTipo().name().equals(d.tipo())
                && c.getPrioridade().name().equals(d.prioridade())
                && Objects.equals(c.getSolicitante(), d.solicitante())
                && Objects.equals(c.getSetor(), d.setor())
                && Objects.equals(c.getTelefone(), d.telefone())
                && Objects.equals(c.getDescricao(), d.descricao());
    }

    private Response resposta(Chamado chamado, HttpStatus status, String mensagem) {
        return Response.builder()
                .status(status.value())
                .mensagem(mensagem)
                .chamado(ChamadoResumoDTO.from(chamado))
                .build();
    }
}
