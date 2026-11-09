package com.aratech.moveis.pcp.producao.service.imp;

import com.aratech.moveis.pcp.producao.dto.Carga;
import com.aratech.moveis.pcp.producao.dto.Linha;
import com.aratech.moveis.pcp.producao.dto.NovaLinha;
import com.aratech.moveis.pcp.producao.dto.NovaOrdem;
import com.aratech.moveis.pcp.producao.dto.Ordem;
import com.aratech.moveis.pcp.producao.repository.ProducaoRepository;
import com.aratech.moveis.pcp.producao.service.ProducaoService;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Indicador;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProducaoServiceImp implements ProducaoService {
    private final OperacaoService base;
    private final ProducaoRepository repository;
    private final com.aratech.moveis.pcp.lotes.LotesProducaoService lotesProducao;

    public Pagina<Linha> linhas(int pagina, int tamanho, String busca) {
        return repository.listarLinhas(pagina, tamanho, busca);
    }

    public Pagina<Ordem> ordens(String filtro, int pagina, int tamanho, String busca) {
        return repository.listarOrdens(filtro, pagina, tamanho, busca);
    }

    public List<Opcao> opcoesLinhas() {
        return repository.listarLinhasAtivas();
    }

    public Ordem buscar(long id) {
        return repository.buscarOrdem(id);
    }

    @Transactional
    public Linha linha(Long id, NovaLinha dto) {
        return base.comando(
                dto.chaveOperacao(),
                "linha:" + id,
                dto,
                Linha.class,
                () -> {
                    Long registro = id;
                    if (registro == null)
                        registro = repository.inserirLinha(dto.nome().trim(), dto.horasDia());
                    else {
                        var atual = repository.bloquearLinhaPorId(registro);
                        base.versao(atual.versao(), dto.versao());
                        base.exigir(
                                repository.contarOrdensAbertasDaLinha(registro) == 0,
                                "Reprograme as ordens abertas antes de mudar a capacidade da"
                                        + " linha.");
                        repository.atualizarLinha(dto.nome().trim(), dto.horasDia(), registro);
                    }
                    base.auditar(
                            "PCP",
                            "SALVAR",
                            "LINHA",
                            registro,
                            "Linha e horas disponíveis cadastradas");
                    return repository.buscarLinha(registro);
                });
    }

    private void periodo(NovaOrdem dto) {
        if (dto.previsao().isBefore(dto.inicio())
                || dto.previsao().isAfter(dto.inicio().plusDays(365)))
            throw new AuthException(
                    400,
                    "Informe previsão igual ou posterior ao início, em um período de até 366"
                            + " dias.");
    }

    private void capacidade(NovaOrdem dto, Long ignorar) {
        if (dto.linhaId() == null) return;
        var l = repository.bloquearLinhaPorId(dto.linhaId());
        base.exigir(l.ativo(), "Linha desativada.");
        BigDecimal dias =
                BigDecimal.valueOf(ChronoUnit.DAYS.between(dto.inicio(), dto.previsao()) + 1);
        BigDecimal diaria = dto.horasPrevistas().divide(dias, 8, RoundingMode.UP);
        var existentes =
                repository.listarReservasDeCapacidade(
                        dto.linhaId(), ignorar, ignorar, dto.previsao(), dto.inicio());
        for (LocalDate dia = dto.inicio(); !dia.isAfter(dto.previsao()); dia = dia.plusDays(1)) {
            BigDecimal carga = diaria;
            for (var o : existentes) {
                var inicio = o.inicio();
                var fim = o.previsao();
                if (!dia.isBefore(inicio) && !dia.isAfter(fim))
                    carga =
                            carga.add(
                                    o.horasPrevistas()
                                            .divide(
                                                    BigDecimal.valueOf(
                                                            ChronoUnit.DAYS.between(inicio, fim)
                                                                    + 1),
                                                    8,
                                                    RoundingMode.UP));
            }
            base.exigir(
                    carga.compareTo(l.horasDia()) <= 0,
                    "A programação excede as horas da linha no dia "
                            + dia
                            + ". Ajuste período, linha ou horas previstas.");
        }
    }

    @Transactional
    public Ordem salvar(Long id, NovaOrdem dto) {
        periodo(dto);
        return base.comando(
                dto.chaveOperacao(),
                "ordem:" + id,
                dto,
                Ordem.class,
                () -> {
                    Long registro = id;
                    if (id != null) {
                        var atual = repository.bloquearOrdemPorId(id);
                        lotesProducao.validarReprogramacao(id,dto.produto(),dto.quantidade());
                        base.versao(atual.versao(), dto.versao());
                        base.exigir(
                                atual.status().equals("PLANEJADA"),
                                "Somente ordens planejadas podem ser reprogramadas.");
                        if (atual.pedidoId() != null) {
                            var original = buscar(id);
                            base.exigir(
                                    original.produto().equals(dto.produto().trim())
                                            && original.quantidade() == dto.quantidade(),
                                    "Produto e quantidade da encomenda vêm do pedido comercial.");
                        }
                    }
                    capacidade(dto, id);
                    if (registro == null)
                        registro =
                                repository.inserirOrdem(
                                        dto.produto().trim(),
                                        dto.quantidade(),
                                        dto.inicio(),
                                        dto.previsao(),
                                        dto.responsavel().trim(),
                                        dto.linhaId(),
                                        dto.horasPrevistas());
                    else
                        repository.atualizarOrdem(
                                dto.produto().trim(),
                                dto.quantidade(),
                                dto.inicio(),
                                dto.previsao(),
                                dto.responsavel().trim(),
                                dto.linhaId(),
                                dto.horasPrevistas(),
                                registro);
                    base.auditar("PCP", "SALVAR", "ORDEM", registro, "Planejamento da ordem");
                    return buscar(registro);
                });
    }

    @Transactional
    public Ordem acao(long id, String acao, Comando dto) {
        return base.comando(
                dto.chaveOperacao(),
                "ordem:" + id + ":" + acao,
                dto,
                Ordem.class,
                () -> {
                    var estado = repository.bloquearOrdemPorId(id);
                    base.versao(estado.versao(), dto.versao());
                    var p = buscar(id);
                    if (acao.equals("iniciar") || acao.equals("concluir"))
                        base.exigir(
                                repository.contarRequisicoesPendentes(id) == 0,
                                "Resolva as requisições pendentes no Almoxarifado antes de iniciar"
                                        + " ou concluir a ordem.");
                    if (acao.equals("cancelar")) {
                        base.exigir(
                                repository.contarRequisicoesAtendidas(id) == 0,
                                "Devolva os materiais já entregues antes de cancelar a ordem"
                                        + " planejada.");
                        repository.atualizarRequisicaoRecusada(id);
                    }
                    switch (acao) {
                        case "iniciar" -> {
                            base.exigir(
                                    p.status().equals("PLANEJADA") && p.linhaId() != null,
                                    "Programe uma linha antes de iniciar uma ordem planejada.");
                            base.exigir(
                                    !p.inicio()
                                            .isAfter(LocalDate.now(ZoneId.of("America/Sao_Paulo"))),
                                    "A ordem não pode ser iniciada antes da data programada.");
                            repository.atualizarOrdemEmProducao(id);
                            if (p.pedidoId() != null)
                                repository.atualizarPedidoComercialEmProducao(p.pedidoId());
                        }
                        case "concluir" -> {
                            lotesProducao.exigirOrigemConcluida(id);
                            base.exigir(
                                    p.status().equals("EM_PRODUCAO"),
                                    "Somente ordem em produção pode ser concluída.");
                            repository.atualizarOrdemFinalizada(id);
                            if (p.pedidoId() != null)
                                repository.atualizarPedidoComercialPronto(p.pedidoId());
                        }
                        case "cancelar" -> {
                            lotesProducao.exigirOrigemSemLotes(id);
                            base.motivo(dto.motivo());
                            base.exigir(
                                    p.status().equals("PLANEJADA") && p.pedidoId() == null,
                                    "Cancele encomendas pelo Comercial; ordens já iniciadas não"
                                            + " podem ser apagadas.");
                            repository.atualizarOrdemCancelada(id);
                        }
                        default -> throw new AuthException(400, "Transição de produção inválida.");
                    }
                    base.auditar(
                            "PCP",
                            acao.toUpperCase(Locale.ROOT),
                            "ORDEM",
                            id,
                            "Ordem #" + id + "; " + base.motivo(dto.motivo()));
                    return buscar(id);
                });
    }

    public List<Carga> capacidade() {
        return repository.consultarCargaDasLinhas();
    }

    public List<Indicador> indicadores() {
        var r = repository.consultarIndicadoresDeProducao();
        return List.of(
                indicador("Ordens planejadas", r.get("planejadas"), "ordens"),
                indicador("Em produção", r.get("andamento"), "ordens"),
                indicador("Ordens atrasadas", r.get("atrasadas"), "ordens"),
                indicador("Produção do mês", r.get("produzidas"), "unidades"),
                indicador("Prontas para expedição", r.get("prontas"), "ordens"));
    }

    private Indicador indicador(String nome, Object valor, String unidade) {
        return new Indicador(
                nome,
                new BigDecimal(valor.toString()),
                unidade,
                "Calculado a partir dos registros de produção.");
    }

    @Override
    @Transactional
    public void criarEncomenda(long pedidoId, String produto, int quantidade, LocalDate entrega) {
        repository.inserirOrdemDaEncomenda(produto, quantidade, entrega, pedidoId);
    }

    @Override
    @Transactional
    public void cancelarEncomenda(long pedidoId) {
        var estados = repository.bloquearOrdensDaEncomenda(pedidoId);
        lotesProducao.exigirPedidoSemLotes(pedidoId);
        base.exigir(estados.stream().allMatch("PLANEJADA"::equals), "A produção já foi iniciada.");
        base.exigir(
                repository.contarMateriaisEntreguesDaEncomenda(pedidoId) == 0,
                "Devolva os materiais já entregues antes de cancelar a encomenda.");
        repository.cancelarOrdensDaEncomenda(pedidoId);
        repository.recusarPendentesDaEncomenda(pedidoId);
    }
}
