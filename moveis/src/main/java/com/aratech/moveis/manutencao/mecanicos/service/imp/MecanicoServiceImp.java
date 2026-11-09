package com.aratech.moveis.manutencao.mecanicos.service.imp;

import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;
import com.aratech.moveis.manutencao.mecanicos.dto.MecanicoDTO;
import com.aratech.moveis.manutencao.mecanicos.dto.MecanicoResumoDTO;
import com.aratech.moveis.manutencao.mecanicos.enums.EspecialidadeMecanico;
import com.aratech.moveis.manutencao.mecanicos.enums.TurnoMecanico;
import com.aratech.moveis.manutencao.mecanicos.model.Mecanico;
import com.aratech.moveis.manutencao.mecanicos.repository.MecanicoRepository;
import com.aratech.moveis.manutencao.mecanicos.service.MecanicoService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class MecanicoServiceImp implements MecanicoService {
    private final MecanicoRepository repository;
    private final ChamadoValidator validator;

    @Override
    @Transactional(readOnly = true)
    public Response listar(int pagina, int tamanho, Boolean ativo) {
        validator.paginacao(pagina, tamanho);
        var pageable = PageRequest.of(pagina, tamanho, Sort.by("nome", "id"));
        var p =
                ativo == null
                        ? repository.findAll(pageable)
                        : repository.findByAtivo(ativo, pageable);
        return Response.builder()
                .status(200)
                .mecanicos(p.getContent().stream().map(MecanicoResumoDTO::from).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(p.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public Response salvar(Long id, MecanicoDTO dto) {
        if (dto == null)
            throw new ChamadoException(HttpStatus.BAD_REQUEST, "Informe os dados do mecanico.");
        String nome = validator.texto(dto.nome(), "Nome", 3, 150, false);
        var turno = validator.valorEnum(TurnoMecanico.class, dto.turno(), "Turno invalido.");
        if (dto.especialidades() == null
                || dto.especialidades().isEmpty()
                || dto.especialidades().size() > EspecialidadeMecanico.values().length)
            throw new ChamadoException(
                    HttpStatus.BAD_REQUEST, "Selecione as especialidades do mecanico.");
        var especialidades = new LinkedHashSet<EspecialidadeMecanico>();
        for (String e : dto.especialidades())
            especialidades.add(
                    validator.valorEnum(EspecialidadeMecanico.class, e, "Especialidade invalida."));
        var m = id == null ? new Mecanico() : buscar(id);
        if (id == null) m.setAtivo(true);
        m.setNome(nome);
        m.setTurno(turno);
        m.setEspecialidades(especialidades);
        return resposta(repository.saveAndFlush(m), id == null ? 201 : 200);
    }

    @Override
    @Transactional
    public Response status(Long id, boolean ativo) {
        var m = buscar(id);
        m.setAtivo(ativo);
        return resposta(repository.saveAndFlush(m), 200);
    }

    private Mecanico buscar(Long id) {
        validator.id(id);
        return repository
                .findByIdForUpdate(id)
                .orElseThrow(
                        () ->
                                new ChamadoException(
                                        HttpStatus.NOT_FOUND, "Mecanico nao encontrado."));
    }

    private Response resposta(Mecanico m, int status) {
        return Response.builder().status(status).mecanico(MecanicoResumoDTO.from(m)).build();
    }
}
