package com.aratech.moveis.administracao.gestao.service.imp;

import com.aratech.moveis.administracao.gestao.dto.Auditoria;
import com.aratech.moveis.administracao.gestao.dto.Parametro;
import com.aratech.moveis.administracao.gestao.dto.ValorParametro;
import com.aratech.moveis.administracao.gestao.repository.AdministracaoRepository;
import com.aratech.moveis.administracao.gestao.service.AdministracaoService;
import com.aratech.moveis.shared.dto.Indicador;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdministracaoServiceImp implements AdministracaoService {
    private final OperacaoService base;
    private final AdministracaoRepository repository;

    public Pagina<Auditoria> auditoria(int pagina, int tamanho, String busca) {
        return repository.listarAuditoria(pagina, tamanho, busca);
    }

    public List<Parametro> parametros() {
        return repository.listarParametros();
    }

    @Transactional
    public Parametro parametro(String chave, ValorParametro dto) {
        return base.comando(
                dto.chaveOperacao(),
                "parametro:" + chave,
                dto,
                Parametro.class,
                () -> {
                    var p = repository.bloquearParametro(chave);
                    base.versao(p.versao(), dto.versao());
                    repository.atualizarParametro(dto.valor(), chave);
                    base.auditar(
                            "ADMINISTRACAO",
                            "ALTERAR",
                            "PARAMETRO",
                            null,
                            "Parâmetro " + chave + " ajustado para " + dto.valor());
                    return repository.buscarParametro(chave);
                });
    }

    public List<Indicador> relatorio() {
        List<Indicador> itens = new ArrayList<>();
        adicionar(itens, "Compras aguardando aprovação", repository.comprasPendentes(), "pedidos");
        adicionar(
                itens,
                "Compras recebidas sem provisão financeira",
                repository.comprasSemProvisao(),
                "pedidos");
        adicionar(itens, "Ordens em atraso", repository.ordensAtrasadas(), "ordens");
        adicionar(
                itens,
                "Produção com prazo próximo",
                repository.producaoProxima(base.parametro("pcp.antecedencia_dias")),
                "ordens");
        adicionar(itens, "Títulos vencidos", repository.titulosVencidos(), "BRL");
        adicionar(
                itens,
                "Pagamentos próximos do vencimento",
                repository.pagamentosProximos(base.parametro("financeiro.antecedencia_dias")),
                "BRL");
        adicionar(
                itens,
                "Afastamentos aguardando decisão",
                repository.afastamentosPendentes(),
                "solicitações");
        return itens;
    }

    private void adicionar(List<Indicador> itens, String titulo, BigDecimal valor, String unidade) {
        itens.add(
                new Indicador(
                        titulo,
                        valor,
                        unidade,
                        "Calculado dos registros persistidos; sem projeção fiscal ou bancária."));
    }
}
