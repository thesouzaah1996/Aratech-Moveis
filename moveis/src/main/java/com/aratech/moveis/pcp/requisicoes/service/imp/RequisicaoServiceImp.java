package com.aratech.moveis.pcp.requisicoes.service.imp;

import com.aratech.moveis.almoxarifado.produtos.dto.MovimentacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.service.ProdutoService;
import com.aratech.moveis.pcp.requisicoes.dto.NovaRequisicao;
import com.aratech.moveis.pcp.requisicoes.dto.Requisicao;
import com.aratech.moveis.pcp.requisicoes.repository.RequisicaoRepository;
import com.aratech.moveis.pcp.requisicoes.service.RequisicaoService;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RequisicaoServiceImp implements RequisicaoService {
    private final OperacaoService base;
    private final RequisicaoRepository repository;
    private final ProdutoService estoque;

    public Pagina<Requisicao> listar(int pagina, int tamanho, String busca) {
        return repository.listarRequisicoes(pagina, tamanho, busca);
    }

    public List<Opcao> ordens() {
        return repository.listarOrdensAbertas();
    }

    public List<Opcao> materiais() {
        return repository.listarMateriaisDisponiveis();
    }

    @Transactional
    public Requisicao criar(NovaRequisicao dto) {
        return base.comando(
                dto.chaveOperacao(),
                "requisicao:criar",
                dto,
                Requisicao.class,
                () -> {
                    base.exigir(
                            !repository.bloquearOrdemAberta(dto.ordemId()).isEmpty(),
                            "A ordem precisa estar planejada ou em produção.");
                    base.exigir(
                            !repository.buscarMaterialAtivo(dto.produtoId()).isEmpty(),
                            "Selecione um material ativo do Almoxarifado.");
                    Long id =
                            repository.inserirRequisicao(
                                    dto.ordemId(),
                                    dto.produtoId(),
                                    dto.quantidade(),
                                    base.motivo(dto.motivo()));
                    base.auditar(
                            "PCP",
                            "SOLICITAR",
                            "REQUISICAO",
                            id,
                            "Material solicitado para OP-" + dto.ordemId());
                    return repository.buscarRequisicao(id);
                });
    }

    @Transactional
    public Requisicao acao(long id, String acao, Comando dto) {
        return base.comando(
                dto.chaveOperacao(),
                "requisicao:" + id + ":" + acao,
                dto,
                Requisicao.class,
                () -> {
                    var r = repository.bloquearRequisicao(id);
                    base.versao(r.versao(), dto.versao());
                    String motivo = base.motivo(dto.motivo());
                    String destino;
                    if (acao.equals("devolver")) {
                        base.exigir(
                                r.status().equals("ATENDIDA"),
                                "Somente material já entregue pode ser devolvido integralmente.");
                        base.exigir(
                                !repository.bloquearOrdemPlanejada(r.ordemId()).isEmpty(),
                                "Devolução por este fluxo exige ordem ainda não iniciada.");
                        estoque.entrada(
                                r.produtoId(),
                                MovimentacaoDTO.builder()
                                        .quantidade(BigDecimal.valueOf(r.quantidade()))
                                        .chaveOperacao(dto.chaveOperacao())
                                        .motivo(
                                                "Devolução da requisição #"
                                                        + id
                                                        + " — "
                                                        + motivo.substring(
                                                                0, Math.min(400, motivo.length())))
                                        .build());
                        destino = "DEVOLVIDA";
                    } else {
                        base.exigir(r.status().equals("PENDENTE"), "A requisição já foi decidida.");
                        base.exigir(
                                !repository.bloquearOrdemAberta(r.ordemId()).isEmpty(),
                                "A ordem não está mais aberta para materiais.");
                        if (acao.equals("atender")) {
                            estoque.saida(
                                    r.produtoId(),
                                    MovimentacaoDTO.builder()
                                            .quantidade(BigDecimal.valueOf(r.quantidade()))
                                            .chaveOperacao(dto.chaveOperacao())
                                            .motivo(
                                                    "Material da OP-"
                                                            + r.ordemId()
                                                            + "; requisição #"
                                                            + id
                                                            + " — "
                                                            + motivo.substring(
                                                                    0,
                                                                    Math.min(350, motivo.length())))
                                            .build());
                            destino = "ATENDIDA";
                        } else if (acao.equals("reprovar")) destino = "RECUSADA";
                        else throw new AuthException(400, "Ação de requisição inválida.");
                    }
                    repository.registrarDecisao(destino, motivo, id);
                    base.auditar(
                            "ALMOXARIFADO",
                            acao.toUpperCase(Locale.ROOT),
                            "REQUISICAO",
                            id,
                            "Decisão integrada ao saldo da OP-" + r.ordemId());
                    return repository.buscarRequisicao(id);
                });
    }
}
