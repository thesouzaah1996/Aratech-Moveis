package com.aratech.moveis.financeiro.titulos.repository.imp;

import com.aratech.moveis.financeiro.titulos.dto.Caixa;
import com.aratech.moveis.financeiro.titulos.dto.Titulo;
import com.aratech.moveis.financeiro.titulos.repository.FinanceiroRepository;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.repository.OperacaoConsultaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FinanceiroRepositoryImp implements FinanceiroRepository {
    private final JdbcTemplate jdbc;
    private final OperacaoConsultaRepository consultas;
    private static final String SELECT =
            "SELECT"
                + " id,tipo,descricao,pessoa,valor,vencimento,documento,emissao,referencia,origem,origem_id,CASE"
                + " WHEN status='PENDENTE' AND vencimento<CURRENT_DATE THEN 'VENCIDO' ELSE status"
                + " END status,baixa,versao FROM financeiro_titulo";

    public Pagina<Titulo> listarTitulos(
            String recurso, int pagina, int tamanho, String busca, String valor) {
        return consultas.pagina(
                SELECT
                        + " WHERE tipo=?"
                        + (recurso.equals("notas") ? " AND documento IS NOT NULL" : ""),
                Titulo.class,
                pagina,
                tamanho,
                busca,
                valor);
    }

    public Titulo buscarTitulo(long id) {
        return consultas.um(SELECT + " WHERE id=?", Titulo.class, id);
    }

    public Titulo bloquearTituloPorId(long id) {
        return consultas.um(
                "SELECT * FROM financeiro_titulo WHERE id=? FOR UPDATE", Titulo.class, id);
    }

    public int vincularDocumento(String documento, LocalDate emissao, long id) {
        return jdbc.update(
                "UPDATE financeiro_titulo SET documento=?,emissao=?,versao=versao+1 WHERE id=?",
                documento,
                emissao,
                id);
    }

    public Long inserirTitulo(
            String tipo,
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento,
            String documento,
            LocalDate emissao,
            String referencia) {
        return jdbc.queryForObject(
                "INSERT INTO"
                    + " financeiro_titulo(tipo,descricao,pessoa,valor,vencimento,documento,emissao,referencia)"
                    + " VALUES (?,?,?,?,?,?,?,?) RETURNING id",
                Long.class,
                tipo,
                descricao,
                pessoa,
                valor,
                vencimento,
                documento,
                emissao,
                referencia);
    }

    public int atualizarTituloManual(
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento,
            String documento,
            LocalDate emissao,
            String referencia,
            Long registro) {
        return jdbc.update(
                "UPDATE financeiro_titulo SET"
                    + " descricao=?,pessoa=?,valor=?,vencimento=?,documento=?,emissao=?,referencia=?,versao=versao+1"
                    + " WHERE id=?",
                descricao,
                pessoa,
                valor,
                vencimento,
                documento,
                emissao,
                referencia,
                registro);
    }

    public int atualizarTituloBaixado(LocalDate data, long id) {
        return jdbc.update(
                "UPDATE financeiro_titulo SET status='BAIXADO',baixa=?,versao=versao+1 WHERE id=?",
                data,
                id);
    }

    public int atualizarTituloCancelado(long id) {
        return jdbc.update(
                "UPDATE financeiro_titulo SET status='CANCELADO',versao=versao+1 WHERE id=?", id);
    }

    public List<Long> inserirProvisao(
            String tipo,
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento,
            String origem,
            long origemId) {
        return jdbc.query(
                "INSERT INTO"
                    + " financeiro_titulo(tipo,descricao,pessoa,valor,vencimento,origem,origem_id)"
                    + " VALUES (?,?,?,?,?,?,?) ON CONFLICT(origem,origem_id) DO NOTHING RETURNING"
                    + " id",
                (r, n) -> r.getLong(1),
                tipo,
                descricao,
                pessoa,
                valor,
                vencimento,
                origem,
                origemId);
    }

    public List<String> bloquearTitulosDaOrigem(String origem, long id) {
        return jdbc.queryForList(
                "SELECT status FROM financeiro_titulo WHERE origem=? AND origem_id=? FOR UPDATE",
                String.class,
                origem,
                id);
    }

    public int cancelarPendentesDaOrigem(String origem, long id) {
        return jdbc.update(
                "UPDATE financeiro_titulo SET status='CANCELADO',versao=versao+1 WHERE origem=? AND"
                        + " origem_id=? AND status='PENDENTE'",
                origem,
                id);
    }

    public Pagina<Caixa> listarCaixa(int pagina, int tamanho, String busca, LocalDate atDay) {
        return consultas.pagina(
                "SELECT id,baixa data,descricao,CASE WHEN tipo='RECEBER' THEN 'ENTRADA' ELSE"
                    + " 'SAIDA' END tipo,valor,SUM(CASE WHEN tipo='RECEBER' THEN valor ELSE -valor"
                    + " END) OVER(ORDER BY baixa,id) saldo_acumulado FROM financeiro_titulo WHERE"
                    + " status='BAIXADO' AND baixa<?",
                Caixa.class,
                pagina,
                tamanho,
                busca,
                atDay);
    }
}
