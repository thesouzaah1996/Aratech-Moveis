package com.aratech.moveis.manutencao.fila.service.imp;

import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.chamados.repository.ChamadoRepository;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;
import com.aratech.moveis.manutencao.fila.dto.AtribuicaoDTO;
import com.aratech.moveis.manutencao.fila.dto.ItemFilaDTO;
import com.aratech.moveis.manutencao.fila.service.FilaChamadosService;
import com.aratech.moveis.manutencao.mecanicos.repository.MecanicoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FilaChamadosServiceImp implements FilaChamadosService {
    private final ChamadoRepository repository;
    private final MecanicoRepository mecanicos;
    private final ChamadoValidator validator;

    @Override
    @Transactional(readOnly = true)
    public Response listar(int pagina, int tamanho, String status, String busca) {
        validator.paginacao(pagina, tamanho);
        var estado = validator.status(status);
        String texto =
                validator
                        .texto(busca == null ? "" : busca, "Busca", 0, 150, false)
                        .toLowerCase(Locale.ROOT);
        Specification<Chamado> filtro =
                (r, q, c) -> {
                    var condicoes = new ArrayList<jakarta.persistence.criteria.Predicate>();
                    if (estado != null) condicoes.add(c.equal(r.get("status"), estado));
                    if (!texto.isEmpty()) {
                        String like =
                                "%"
                                        + texto.replace("!", "!!")
                                                .replace("%", "!%")
                                                .replace("_", "!_")
                                        + "%";
                        var termos = new ArrayList<jakarta.persistence.criteria.Predicate>();
                        for (String campo :
                                List.of(
                                        "equipamento",
                                        "solicitante",
                                        "setor",
                                        "nomePeca",
                                        "codigoPeca"))
                            termos.add(c.like(c.lower(r.get(campo)), like, '!'));
                        if (texto.matches("[0-9]{1,18}"))
                            termos.add(c.equal(r.get("id"), Long.valueOf(texto)));
                        condicoes.add(
                                c.or(
                                        termos.toArray(
                                                jakarta.persistence.criteria.Predicate[]::new)));
                    }
                    if (!Long.class.equals(q.getResultType())
                            && !long.class.equals(q.getResultType())) {
                        var prioridade =
                                c.<Integer>selectCase()
                                        .when(
                                                c.equal(
                                                        r.get("prioridade"),
                                                        PrioridadeChamado.ALTA),
                                                0)
                                        .when(
                                                c.equal(
                                                        r.get("prioridade"),
                                                        PrioridadeChamado.MEDIA),
                                                1)
                                        .otherwise(2);
                        q.orderBy(
                                c.asc(prioridade),
                                c.asc(r.get("dataAbertura")),
                                c.asc(r.get("id")));
                    }
                    return c.and(condicoes.toArray(jakarta.persistence.criteria.Predicate[]::new));
                };
        var p = repository.findAll(filtro, PageRequest.of(pagina, tamanho));
        return Response.builder()
                .status(200)
                .itens(p.getContent().stream().map(ItemFilaDTO::from).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(p.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response buscar(Long id) {
        validator.id(id);
        return resposta(repository.findById(id).orElseThrow(() -> ausente()));
    }

    @Override
    @Transactional
    public Response atribuir(Long id, AtribuicaoDTO dto) {
        if (dto == null)
            throw new ChamadoException(HttpStatus.BAD_REQUEST, "Selecione o mecanico responsavel.");
        validator.id(dto.mecanicoId());
        var chamado = buscarBloqueado(id);
        if (chamado.getStatus() == StatusChamado.CONCLUIDA)
            throw new ChamadoException(
                    HttpStatus.CONFLICT, "Chamado concluido nao pode ser reatribuido.");
        var mecanico =
                mecanicos
                        .findByIdForUpdate(dto.mecanicoId())
                        .orElseThrow(
                                () ->
                                        new ChamadoException(
                                                HttpStatus.NOT_FOUND, "Mecanico nao encontrado."));
        if (!mecanico.isAtivo())
            throw new ChamadoException(HttpStatus.CONFLICT, "Selecione um mecanico ativo.");
        if (chamado.getStatus() == StatusChamado.EM_MANUTENCAO
                && chamado.getMecanico() != null
                && chamado.getMecanico().getId().equals(mecanico.getId())) return resposta(chamado);
        chamado.setMecanico(mecanico);
        chamado.setStatus(StatusChamado.EM_MANUTENCAO);
        if (chamado.getDataInicio() == null)
            chamado.setDataInicio(Instant.now().truncatedTo(ChronoUnit.MICROS));
        return resposta(repository.saveAndFlush(chamado));
    }

    @Override
    @Transactional
    public Response concluir(Long id) {
        var chamado = buscarBloqueado(id);
        if (chamado.getStatus() == StatusChamado.CONCLUIDA) return resposta(chamado);
        if (chamado.getStatus() != StatusChamado.EM_MANUTENCAO || chamado.getMecanico() == null)
            throw new ChamadoException(
                    HttpStatus.CONFLICT, "Atribua um mecanico antes de concluir o atendimento.");
        chamado.setStatus(StatusChamado.CONCLUIDA);
        chamado.setDataConclusao(Instant.now().truncatedTo(ChronoUnit.MICROS));
        return resposta(repository.saveAndFlush(chamado));
    }

    private Chamado buscarBloqueado(Long id) {
        validator.id(id);
        return repository.findByIdForUpdate(id).orElseThrow(() -> ausente());
    }

    private ChamadoException ausente() {
        return new ChamadoException(HttpStatus.NOT_FOUND, "Chamado nao encontrado.");
    }

    private Response resposta(Chamado c) {
        return Response.builder().status(200).item(ItemFilaDTO.from(c)).build();
    }
}
