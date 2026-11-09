package com.aratech.moveis.suporte.chamados.service.imp;

import com.aratech.moveis.suporte.chamados.dto.ChamadoTiDTO;
import com.aratech.moveis.suporte.chamados.dto.ChamadoTiResumoDTO;
import com.aratech.moveis.suporte.chamados.dto.Response;
import com.aratech.moveis.suporte.chamados.enums.PrioridadeTi;
import com.aratech.moveis.suporte.chamados.enums.TipoChamadoTi;
import com.aratech.moveis.suporte.chamados.exception.ChamadoTiException;
import com.aratech.moveis.suporte.chamados.model.ChamadoTi;
import com.aratech.moveis.suporte.chamados.repository.ChamadoTiRepository;
import com.aratech.moveis.suporte.chamados.service.ChamadoTiService;
import com.aratech.moveis.suporte.chamados.validation.ChamadoTiValidator;

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
public class ChamadoTiServiceImp implements ChamadoTiService {
    private final ChamadoTiRepository repository;
    private final ChamadoTiValidator validator;

    @Override
    @Transactional
    public Response abrir(ChamadoTiDTO dto) {
        var d = validator.validar(dto);
        var anterior = repository.findByChaveOperacao(d.chaveOperacao());
        if (anterior.isPresent()) {
            if (!mesmosDados(anterior.get(), d))
                throw new ChamadoTiException(
                        HttpStatus.CONFLICT, "Chave da operacao ja utilizada com outros dados.");
            return resposta(anterior.get(), 201, "Chamado ja registrado.");
        }
        var c =
                ChamadoTi.builder()
                        .tipo(validator.rotulo(TipoChamadoTi.class, d.tipo(), "Tipo invalido."))
                        .prioridade(
                                validator.rotulo(
                                        PrioridadeTi.class, d.prioridade(), "Prioridade invalida."))
                        .moduloAfetado(d.moduloAfetado())
                        .titulo(d.titulo())
                        .solicitante(d.solicitante())
                        .setor(d.setor())
                        .contato(d.contato())
                        .descricao(d.descricao())
                        .tentativasSolucao(d.tentativasSolucao())
                        .chaveOperacao(d.chaveOperacao())
                        .dataAbertura(Instant.now().truncatedTo(ChronoUnit.MICROS))
                        .build();
        return resposta(repository.saveAndFlush(c), 201, "Chamado de TI registrado com sucesso.");
    }

    @Override
    @Transactional(readOnly = true)
    public Response listar(int pagina, int tamanho, String status, String busca) {
        validator.paginacao(pagina, tamanho);
        var estado = validator.status(status);
        String texto =
                validator
                        .texto(busca == null ? "" : busca, "Busca", 0, 150, false)
                        .toLowerCase(Locale.ROOT);
        Specification<ChamadoTi> filtro =
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
                                        "numero",
                                        "titulo",
                                        "solicitante",
                                        "setor",
                                        "moduloAfetado"))
                            termos.add(c.like(c.lower(r.get(campo)), like, '!'));
                        for (var tipo : TipoChamadoTi.values())
                            if (tipo.toString().toLowerCase(Locale.ROOT).contains(texto))
                                termos.add(c.equal(r.get("tipo"), tipo));
                        condicoes.add(
                                c.or(
                                        termos.toArray(
                                                jakarta.persistence.criteria.Predicate[]::new)));
                    }
                    if (!Long.class.equals(q.getResultType())
                            && !long.class.equals(q.getResultType())) {
                        var prioridade =
                                c.<Integer>selectCase()
                                        .when(c.equal(r.get("prioridade"), PrioridadeTi.CRITICA), 0)
                                        .when(c.equal(r.get("prioridade"), PrioridadeTi.ALTA), 1)
                                        .when(c.equal(r.get("prioridade"), PrioridadeTi.MEDIA), 2)
                                        .otherwise(3);
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
                .chamados(p.getContent().stream().map(ChamadoTiResumoDTO::from).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(p.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response buscar(Long id) {
        validator.id(id);
        var c =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ChamadoTiException(
                                                HttpStatus.NOT_FOUND,
                                                "Chamado de TI nao encontrado."));
        return resposta(c, 200, "Chamado encontrado.");
    }

    private boolean mesmosDados(ChamadoTi c, ChamadoTiDTO d) {
        return c.getTipo().toString().equals(d.tipo())
                && c.getPrioridade().toString().equals(d.prioridade())
                && Objects.equals(c.getModuloAfetado(), d.moduloAfetado())
                && Objects.equals(c.getTitulo(), d.titulo())
                && Objects.equals(c.getSolicitante(), d.solicitante())
                && Objects.equals(c.getSetor(), d.setor())
                && Objects.equals(c.getContato(), d.contato())
                && Objects.equals(c.getDescricao(), d.descricao())
                && Objects.equals(c.getTentativasSolucao(), d.tentativasSolucao());
    }

    private Response resposta(ChamadoTi c, int status, String mensagem) {
        return Response.builder()
                .status(status)
                .mensagem(mensagem)
                .chamado(ChamadoTiResumoDTO.from(c))
                .build();
    }
}
