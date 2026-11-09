package com.aratech.moveis.portaria.controleacesso.service.imp;

import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.dto.FilaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.enums.StatusAcesso;
import com.aratech.moveis.portaria.controleacesso.exception.ControleAcessoException;
import com.aratech.moveis.portaria.controleacesso.model.RegistroChegada;
import com.aratech.moveis.portaria.controleacesso.repository.RegistroChegadaRepository;
import com.aratech.moveis.portaria.controleacesso.service.ControleAcessoService;
import com.aratech.moveis.portaria.controleacesso.validation.ChegadaValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ControleAcessoServiceImp implements ControleAcessoService {
    private final RegistroChegadaRepository repository;
    private final ChegadaValidator validator;
    private static final String SETOR = "ALMOXARIFADO";

    @Override
    @Transactional
    public ChegadaResumoDTO registrar(ChegadaDTO dto) {
        var dados = validator.validar(dto, true);
        var anterior = repository.findByChaveOperacao(dados.chaveOperacao());
        if (anterior.isPresent()) {
            if (!mesmosDados(anterior.get(), dados))
                throw conflito("Chave de operacao ja utilizada com outros dados.");
            return ChegadaResumoDTO.from(anterior.get());
        }
        duplicidade(dados, 0L);
        var registro =
                RegistroChegada.builder()
                        .chaveOperacao(dados.chaveOperacao())
                        .dataChegada(Instant.now().truncatedTo(ChronoUnit.MICROS))
                        .build();
        preencher(registro, dados);
        return ChegadaResumoDTO.from(repository.saveAndFlush(registro));
    }

    @Override
    @Transactional
    public ChegadaResumoDTO editar(Long id, ChegadaDTO dto) {
        validator.id(id);
        var dados = validator.validar(dto, false);
        var registro = buscar(id);
        if (registro.getStatus() != StatusAcesso.AGUARDANDO)
            throw conflito("Somente chegadas aguardando autorizacao podem ser editadas.");
        duplicidade(dados, id);
        preencher(registro, dados);
        return ChegadaResumoDTO.from(repository.saveAndFlush(registro));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChegadaResumoDTO> fila(int pagina, int tamanho) {
        return consultarFila(
                pagina,
                tamanho,
                null,
                EnumSet.of(
                        StatusAcesso.AGUARDANDO,
                        StatusAcesso.AUTORIZADO,
                        StatusAcesso.EM_RECEBIMENTO));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChegadaResumoDTO> filaRecebimento(int pagina, int tamanho) {
        return consultarFila(
                pagina,
                tamanho,
                SETOR,
                EnumSet.of(
                        StatusAcesso.AGUARDANDO,
                        StatusAcesso.AUTORIZADO,
                        StatusAcesso.EM_RECEBIMENTO));
    }

    private Page<ChegadaResumoDTO> consultarFila(
            int pagina, int tamanho, String setor, Set<StatusAcesso> estados) {
        validator.paginacao(pagina, tamanho);
        Specification<RegistroChegada> filtro =
                (r, q, c) ->
                        c.and(
                                (setor == null
                                        ? c.conjunction()
                                        : c.equal(r.get("setorResponsavel"), setor)),
                                r.get("status").in(estados));
        return repository
                .findAll(filtro, PageRequest.of(pagina, tamanho, Sort.by("dataChegada", "id")))
                .map(ChegadaResumoDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChegadaResumoDTO> historico(
            int pagina, int tamanho, String notaFiscal, LocalDate data) {
        return consultarHistorico(pagina, tamanho, notaFiscal, data, false, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChegadaResumoDTO> historicoRecebimento(int pagina, int tamanho) {
        return consultarHistorico(pagina, tamanho, null, null, true, SETOR);
    }

    private Page<ChegadaResumoDTO> consultarHistorico(
            int pagina,
            int tamanho,
            String notaFiscal,
            LocalDate data,
            boolean recebimento,
            String setor) {
        validator.paginacao(pagina, tamanho);
        String nf = validator.notaFiltro(notaFiscal);
        Set<StatusAcesso> estados =
                recebimento
                        ? EnumSet.of(StatusAcesso.FINALIZADO)
                        : EnumSet.of(StatusAcesso.FINALIZADO);
        String campoData = recebimento ? "dataFinalizacao" : "dataEntrada";
        Specification<RegistroChegada> filtro =
                (r, q, c) -> {
                    var condicoes = new ArrayList<jakarta.persistence.criteria.Predicate>();
                    if (setor != null) condicoes.add(c.equal(r.get("setorResponsavel"), setor));
                    condicoes.add(r.get("status").in(estados));
                    if (!nf.isEmpty()) condicoes.add(c.like(r.get("notaFiscal"), "%" + nf + "%"));
                    if (data != null) {
                        var zona = ZoneId.of("America/Sao_Paulo");
                        condicoes.add(
                                c.greaterThanOrEqualTo(
                                        r.get(campoData), data.atStartOfDay(zona).toInstant()));
                        condicoes.add(
                                c.lessThan(
                                        r.get(campoData),
                                        data.plusDays(1).atStartOfDay(zona).toInstant()));
                    }
                    return c.and(condicoes.toArray(jakarta.persistence.criteria.Predicate[]::new));
                };
        return repository
                .findAll(
                        filtro,
                        PageRequest.of(
                                pagina, tamanho, Sort.by(Sort.Direction.DESC, campoData, "id")))
                .map(ChegadaResumoDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public FilaResumoDTO resumo() {
        return resumoSetor(null);
    }

    @Transactional(readOnly = true)
    public FilaResumoDTO resumoSetor(String setor) {
        var ultimo =
                (setor == null
                        ? repository.findFirstByOrderByIdDesc()
                        : repository.findFirstBySetorResponsavelOrderByIdDesc(setor));
        return new FilaResumoDTO(
                (setor == null
                        ? repository.countByStatus(StatusAcesso.AGUARDANDO)
                        : repository.countBySetorResponsavelAndStatus(
                                setor, StatusAcesso.AGUARDANDO)),
                (setor == null
                        ? repository.countByStatus(StatusAcesso.AUTORIZADO)
                        : repository.countBySetorResponsavelAndStatus(
                                setor, StatusAcesso.AUTORIZADO)),
                (setor == null
                        ? repository.countByStatus(StatusAcesso.EM_RECEBIMENTO)
                        : repository.countBySetorResponsavelAndStatus(
                                setor, StatusAcesso.EM_RECEBIMENTO)),
                ultimo.map(RegistroChegada::getId).orElse(null),
                ultimo.map(RegistroChegada::getEmpresa).orElse(null));
    }

    @Override
    @Transactional
    public ChegadaResumoDTO autorizar(Long id) {
        return transicao(id, StatusAcesso.AGUARDANDO, StatusAcesso.AUTORIZADO);
    }

    @Override
    @Transactional
    public ChegadaResumoDTO confirmarEntrada(Long id) {
        return transicao(id, StatusAcesso.AUTORIZADO, StatusAcesso.EM_RECEBIMENTO);
    }

    @Override
    @Transactional
    public ChegadaResumoDTO finalizar(Long id) {
        return transicao(id, StatusAcesso.EM_RECEBIMENTO, StatusAcesso.FINALIZADO);
    }

    private ChegadaResumoDTO transicao(Long id, StatusAcesso origem, StatusAcesso destino) {
        validator.id(id);
        var registro = buscar(id);
        if (registro.getStatus() == destino) return ChegadaResumoDTO.from(registro);
        if (destino == StatusAcesso.EM_RECEBIMENTO
                && registro.getStatus() == StatusAcesso.AGUARDANDO)
            throw conflito(
                    "Entrada bloqueada: aguarde a autorizacao do setor responsavel antes de liberar"
                            + " o caminhao.");
        if (registro.getStatus() != origem)
            throw conflito(
                    "Etapa invalida. Autorize no setor responsavel, confirme a entrada na portaria"
                            + " e depois finalize o recebimento.");
        Instant agora = Instant.now().truncatedTo(ChronoUnit.MICROS);
        registro.setStatus(destino);
        switch (destino) {
            case AUTORIZADO -> registro.setDataAutorizacao(agora);
            case EM_RECEBIMENTO -> registro.setDataEntrada(agora);
            case FINALIZADO -> registro.setDataFinalizacao(agora);
            default -> throw conflito("Etapa invalida.");
        }
        return ChegadaResumoDTO.from(repository.saveAndFlush(registro));
    }

    private RegistroChegada buscar(Long id) {
        var registro =
                repository
                        .findByIdForUpdate(id)
                        .orElseThrow(
                                () ->
                                        new ControleAcessoException(
                                                HttpStatus.NOT_FOUND, "Chegada nao encontrada."));

        return registro;
    }

    @Transactional(readOnly = true)
    public Page<ChegadaResumoDTO> filaSetor(String setor, int pagina, int tamanho) {
        return consultarFila(
                pagina,
                tamanho,
                setor,
                EnumSet.of(
                        StatusAcesso.AGUARDANDO,
                        StatusAcesso.AUTORIZADO,
                        StatusAcesso.EM_RECEBIMENTO));
    }

    @Transactional(readOnly = true)
    public Page<ChegadaResumoDTO> historicoSetor(String setor, int pagina, int tamanho) {
        return consultarHistorico(pagina, tamanho, null, null, true, setor);
    }

    @Transactional
    public ChegadaResumoDTO editarSetor(String setor, Long id, ChegadaDTO dto) {
        validarSetor(setor, id);
        if (!setor.equals(dto.setorResponsavel()))
            throw conflito("O setor responsavel nao pode ser alterado nesta tela.");
        return editar(id, dto);
    }

    @Transactional
    public ChegadaResumoDTO autorizarSetor(String setor, Long id) {
        validarSetor(setor, id);
        return autorizar(id);
    }

    @Transactional
    public ChegadaResumoDTO finalizarSetor(String setor, Long id) {
        validarSetor(setor, id);
        return finalizar(id);
    }

    private void validarSetor(String setor, Long id) {
        validator.id(id);
        if (!setor.equals(buscar(id).getSetorResponsavel()))
            throw conflito("Este registro nao pertence ao setor responsavel.");
    }

    private void duplicidade(ChegadaDTO dto, Long id) {
        if (repository.existsByNotaFiscalAndEmpresaIgnoreCaseAndIdNot(
                dto.notaFiscal(), dto.empresa(), id))
            throw conflito("Esta nota fiscal ja foi registrada para a empresa.");
        if (repository.existsByPlacaAndStatusNotAndIdNot(dto.placa(), StatusAcesso.FINALIZADO, id))
            throw conflito("Ja existe uma chegada em andamento para esta placa.");
    }

    private boolean mesmosDados(RegistroChegada r, ChegadaDTO d) {
        return Objects.equals(r.getNotaFiscal(), d.notaFiscal())
                && Objects.equals(r.getEmpresa(), d.empresa())
                && Objects.equals(r.getNomeMotorista(), d.nomeMotorista())
                && Objects.equals(r.getPlaca(), d.placa())
                && Objects.equals(r.getDescricaoCarga(), d.descricaoCarga())
                && Objects.equals(r.getSetorResponsavel(), d.setorResponsavel())
                && Objects.equals(r.getTipoOperacao(), d.tipoOperacao());
    }

    private void preencher(RegistroChegada r, ChegadaDTO d) {
        r.setNotaFiscal(d.notaFiscal());
        r.setEmpresa(d.empresa());
        r.setNomeMotorista(d.nomeMotorista());
        r.setPlaca(d.placa());
        r.setDescricaoCarga(d.descricaoCarga());
        r.setSetorResponsavel(d.setorResponsavel());
        r.setTipoOperacao(d.tipoOperacao());
    }

    private ControleAcessoException conflito(String mensagem) {
        return new ControleAcessoException(HttpStatus.CONFLICT, mensagem);
    }
}
