package com.aratech.moveis.financeiro.titulos.service.imp;

import com.aratech.moveis.financeiro.titulos.dto.Caixa;
import com.aratech.moveis.financeiro.titulos.dto.Documento;
import com.aratech.moveis.financeiro.titulos.dto.NovoTitulo;
import com.aratech.moveis.financeiro.titulos.dto.Titulo;
import com.aratech.moveis.financeiro.titulos.repository.FinanceiroRepository;
import com.aratech.moveis.financeiro.titulos.service.FinanceiroService;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinanceiroServiceImp implements FinanceiroService {
    private final OperacaoService base;
    private final FinanceiroRepository repository;

    public String tipo(String recurso) {
        return switch (recurso) {
            case "pagar", "notas" -> "PAGAR";
            case "receber" -> "RECEBER";
            default -> throw new AuthException(404, "Recurso financeiro não encontrado.");
        };
    }

    public Pagina<Titulo> listar(String recurso, int pagina, int tamanho, String busca) {
        return repository.listarTitulos(recurso, pagina, tamanho, busca, tipo(recurso));
    }

    public Titulo buscar(long id) {
        return repository.buscarTitulo(id);
    }

    @Transactional
    public Titulo documentar(String recurso, long id, Documento dto) {
        String tipo = tipo(recurso);
        return base.comando(
                dto.chaveOperacao(),
                "documentar:" + id,
                dto,
                Titulo.class,
                () -> {
                    var p = repository.bloquearTituloPorId(id);
                    base.versao(p.versao(), dto.versao());
                    base.exigir(
                            p.tipo().equals(tipo) && p.status().equals("PENDENTE"),
                            "Somente título pendente pode receber documento.");
                    if (dto.emissao().isAfter(p.vencimento()))
                        throw new AuthException(
                                400, "Emissão não pode ser posterior ao vencimento.");
                    repository.vincularDocumento(dto.documento().trim(), dto.emissao(), id);
                    base.auditar(
                            "FINANCEIRO",
                            "DOCUMENTAR",
                            "TITULO",
                            id,
                            "Documento vinculado ao mesmo título, sem duplicar a obrigação.");
                    return buscar(id);
                });
    }

    @Transactional
    public Titulo salvar(String recurso, Long id, NovoTitulo dto) {
        String tipo = tipo(recurso);
        if (dto.emissao() != null && dto.vencimento().isBefore(dto.emissao()))
            throw new AuthException(400, "Vencimento deve ser igual ou posterior à emissão.");
        String documento =
                dto.documento() == null || dto.documento().isBlank()
                        ? null
                        : dto.documento().trim();
        if (recurso.equals("notas") && (documento == null || dto.emissao() == null))
            throw new AuthException(400, "Informe número do documento e emissão.");
        return base.comando(
                dto.chaveOperacao(),
                "financeiro:" + recurso + ":" + id,
                dto,
                Titulo.class,
                () -> {
                    Long registro = id;
                    if (registro == null)
                        registro =
                                repository.inserirTitulo(
                                        tipo,
                                        dto.descricao().trim(),
                                        dto.pessoa().trim(),
                                        dto.valor(),
                                        dto.vencimento(),
                                        documento,
                                        dto.emissao(),
                                        dto.referencia());
                    else {
                        var atual = repository.bloquearTituloPorId(registro);
                        base.versao(atual.versao(), dto.versao());
                        base.exigir(
                                atual.tipo().equals(tipo)
                                        && atual.status().equals("PENDENTE")
                                        && atual.origem().equals("MANUAL"),
                                "Somente títulos manuais pendentes podem ser editados.");
                        repository.atualizarTituloManual(
                                dto.descricao().trim(),
                                dto.pessoa().trim(),
                                dto.valor(),
                                dto.vencimento(),
                                documento,
                                dto.emissao(),
                                dto.referencia(),
                                registro);
                    }
                    base.auditar(
                            "FINANCEIRO",
                            id == null ? "CADASTRAR" : "ALTERAR",
                            "TITULO",
                            registro,
                            "Título " + tipo + " registrado");
                    return buscar(registro);
                });
    }

    @Transactional
    public Titulo acao(String recurso, long id, String acao, Comando dto) {
        String tipo = tipo(recurso);
        return base.comando(
                dto.chaveOperacao(),
                "titulo:" + id + ":" + acao,
                dto,
                Titulo.class,
                () -> {
                    var p = repository.bloquearTituloPorId(id);
                    base.versao(p.versao(), dto.versao());
                    base.exigir(
                            p.tipo().equals(tipo) && p.status().equals("PENDENTE"),
                            "Título não permite esta operação.");
                    if (acao.equals("baixar")) {
                        if (dto.data() == null
                                || dto.data().isAfter(LocalDate.now(ZoneId.of("America/Sao_Paulo")))
                                || p.emissao() != null && dto.data().isBefore(p.emissao()))
                            throw new AuthException(
                                    400,
                                    "Informe data real da baixa, sem data futura ou anterior à"
                                            + " emissão.");
                        repository.atualizarTituloBaixado(dto.data(), id);
                    } else if (acao.equals("cancelar")) {
                        base.exigir(
                                p.origem().equals("MANUAL"),
                                "Cancele a origem antes de alterar um título integrado.");
                        base.motivo(dto.motivo());
                        repository.atualizarTituloCancelado(id);
                    } else throw new AuthException(400, "Ação financeira inválida.");
                    base.auditar(
                            "FINANCEIRO",
                            acao.toUpperCase(Locale.ROOT),
                            "TITULO",
                            id,
                            "Título " + tipo + "; " + base.motivo(dto.motivo()));
                    return buscar(id);
                });
    }

    // Chamado dentro da transação do setor de origem; restrição única impede dupla provisão.
    public void integrar(
            String origem,
            long origemId,
            String tipo,
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento) {
        integrar(origem, origemId, tipo, descricao, pessoa, valor, vencimento, base.autor());
    }

    public void integrar(
            String origem,
            long origemId,
            String tipo,
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento,
            UsuarioSessao autor) {
        if (valor == null
                || valor.signum() <= 0
                || valor.compareTo(new BigDecimal("9999999999999.99")) > 0
                || vencimento == null
                || pessoa == null
                || pessoa.isBlank())
            throw new AuthException(
                    409,
                    "Complete fornecedor/cliente, valor e vencimento antes de confirmar a origem.");
        Long id =
                repository
                        .inserirProvisao(
                                tipo, descricao, pessoa, valor, vencimento, origem, origemId)
                        .stream()
                        .findFirst()
                        .orElse(null);
        if (id != null)
            base.auditar(
                    autor,
                    "FINANCEIRO",
                    "INTEGRAR",
                    "TITULO",
                    id,
                    "Provisão automática a partir de " + origem + " #" + origemId);
    }

    public void cancelarOrigem(String origem, long id) {
        var titulos = repository.bloquearTitulosDaOrigem(origem, id);
        base.exigir(
                titulos.stream().noneMatch("BAIXADO"::equals),
                "A origem tem título já baixado. Resolva a situação com o Financeiro.");
        repository.cancelarPendentesDaOrigem(origem, id);
    }

    public Pagina<Caixa> caixa(String mes, int pagina, int tamanho, String busca) {
        YearMonth periodo;
        try {
            periodo = YearMonth.parse(mes);
            if (periodo.getYear() < 1900 || periodo.getYear() > 2100)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new AuthException(400, "Informe mês no formato YYYY-MM, entre 1900 e 2100.");
        }
        return repository.listarCaixa(pagina, tamanho, busca, periodo.plusMonths(1).atDay(1));
    }
}
