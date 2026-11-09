package com.aratech.moveis.assistencia.lotes.service.imp;

import com.aratech.moveis.assistencia.lotes.model.LoteAssistencia;
import com.aratech.moveis.assistencia.lotes.repository.LoteAssistenciaRepository;
import com.aratech.moveis.assistencia.lotes.service.LoteAssistenciaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LoteAssistenciaServiceImp implements LoteAssistenciaService {
    private final LoteAssistenciaRepository repository;

    public List<LoteAssistencia> listar() {
        return repository.findAllByOrderByDataEntradaAscIdAsc();
    }

    @Transactional
    public LoteAssistencia receber(LoteAssistencia entrada) {
        if (entrada.getOrigemId() == null
                || entrada.getCodigo() == null
                || entrada.getCodigo().isBlank()
                || entrada.getProduto() == null
                || entrada.getProduto().isBlank()
                || entrada.getQuantidade() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe origem, código, produto e quantidade válida.");
        }
        for (String campo :
                new String[] {
                    entrada.getCodigo(),
                    entrada.getProduto(),
                    entrada.getEmpresa(),
                    entrada.getResponsavel(),
                    entrada.getDefeito()
                })
            if (campo != null && campo.length() > 255)
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Os campos de texto do lote aceitam até 255 caracteres.");
        var existente = repository.findByOrigemId(entrada.getOrigemId());
        if (existente.isPresent()) {
            var atual = existente.get();
            if (!Objects.equals(atual.getCodigo(), entrada.getCodigo())
                    || !Objects.equals(atual.getProduto(), entrada.getProduto())
                    || atual.getQuantidade() != entrada.getQuantidade()
                    || !Objects.equals(atual.getEmpresa(), entrada.getEmpresa())
                    || !Objects.equals(atual.getResponsavel(), entrada.getResponsavel())
                    || !Objects.equals(atual.getDefeito(), entrada.getDefeito()))
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Referência de recebimento já utilizada com outros dados.");
            return atual;
        }
        return existente.orElseGet(
                () -> {
                    var lote = new LoteAssistencia();
                    lote.setOrigemId(entrada.getOrigemId());
                    lote.setCodigo(entrada.getCodigo());
                    lote.setProduto(entrada.getProduto());
                    lote.setQuantidade(entrada.getQuantidade());
                    lote.setEmpresa(entrada.getEmpresa());
                    lote.setResponsavel(entrada.getResponsavel());
                    lote.setDefeito(entrada.getDefeito());
                    lote.setDataEntrada(LocalDate.now(ZoneId.of("America/Sao_Paulo")));
                    lote.setStatus("Não iniciado");
                    return repository.save(lote);
                });
    }

    @Transactional
    public LoteAssistencia avancar(Long id, String acao) {
        var lote =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Lote não encontrado."));
        String esperado =
                switch (acao) {
                    case "iniciar" -> "Não iniciado";
                    case "finalizar" -> "Em andamento";
                    case "carregar" -> "Finalizado";
                    default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                };
        if (!esperado.equals(lote.getStatus()) || lote.getCarregadoEm() != null)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "O lote não permite esta ação no status atual.");
        switch (acao) {
            case "iniciar" -> {
                lote.setStatus("Em andamento");
                lote.setIniciadoEm(Instant.now());
            }
            case "finalizar" -> {
                lote.setStatus("Finalizado");
                lote.setFinalizadoEm(Instant.now());
            }
            case "carregar" -> lote.setCarregadoEm(Instant.now());
        }
        return repository.save(lote);
    }
}
