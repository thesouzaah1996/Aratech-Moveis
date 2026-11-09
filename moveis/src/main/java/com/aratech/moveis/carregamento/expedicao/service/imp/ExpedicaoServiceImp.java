package com.aratech.moveis.carregamento.expedicao.service.imp;

import com.aratech.moveis.carregamento.expedicao.repository.ExpedicaoRepository;
import com.aratech.moveis.carregamento.expedicao.service.ExpedicaoService;
import com.aratech.moveis.pcp.producao.dto.Ordem;
import com.aratech.moveis.pcp.producao.service.ProducaoService;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpedicaoServiceImp implements ExpedicaoService {
    @Override
    public Pagina<Ordem> ordens(String filtro, int pagina, int tamanho, String busca) {
        return producao.ordens(filtro, pagina, tamanho, busca);
    }

    private final OperacaoService base;
    private final ProducaoService producao;
    private final ExpedicaoRepository repository;

    public List<Opcao> chegadas() {
        return repository.listarCaminhoesEmAtendimento();
    }

    @Transactional
    public Ordem expedir(long id, Comando dto) {
        return base.comando(
                dto.chaveOperacao(),
                "expedir:" + id,
                dto,
                Ordem.class,
                () -> {
                    var estado = repository.bloquearOrdemParaExpedicao(id);
                    base.versao(estado.versao(), dto.versao());
                    var p = producao.buscar(id);
                    base.exigir(
                            p.status().equals("FINALIZADA") && p.expedidoEm() == null,
                            "Somente ordem concluída ainda não expedida pode sair.");
                    if (dto.referenciaId() == null)
                        throw new AuthException(
                                400, "Selecione o caminhão com entrada confirmada na Portaria.");
                    var chegada = repository.bloquearCaminhaoEmAtendimento(dto.referenciaId());
                    base.exigir(
                            !chegada.isEmpty(),
                            "A chegada precisa estar em atendimento, com entrada confirmada pela"
                                    + " Portaria.");
                    repository.registrarExpedicao(dto.referenciaId(), id);
                    if (p.pedidoId() != null)
                        repository.atualizarPedidoComercialExpedido(p.pedidoId());
                    base.auditar(
                            "CARREGAMENTO",
                            "EXPEDIR",
                            "ORDEM",
                            id,
                            "Saída integral vinculada à chegada #"
                                    + dto.referenciaId()
                                    + "; "
                                    + base.motivo(dto.motivo()));
                    return producao.buscar(id);
                });
    }
}
