package com.aratech.moveis.comercial.pedidos.repository.imp;

import com.aratech.moveis.comercial.pedidos.dto.Venda;
import com.aratech.moveis.comercial.pedidos.repository.ComercialRepository;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.repository.OperacaoConsultaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ComercialRepositoryImp implements ComercialRepository {
    private final JdbcTemplate jdbc;
    private final OperacaoConsultaRepository consultas;
    static final String ORDENS =
            "SELECT o.id,o.produto,o.quantidade,o.inicio,o.previsao,o.responsavel,o.linha_id,l.nome"
                + " linha,o.horas_previstas,o.pedido_id,o.status,o.concluido_em,o.expedido_em,o.versao"
                + " FROM pcp_ordem o LEFT JOIN pcp_linha l ON l.id=o.linha_id";
    static final String VENDAS =
            "SELECT id,cliente,produto,quantidade,valor_unitario,entrega,vencimento,status,versao"
                    + " FROM comercial_pedido";

    public Pagina<Venda> listarVendas(int pagina, int tamanho, String busca) {
        return consultas.pagina(VENDAS, Venda.class, pagina, tamanho, busca);
    }

    public Venda bloquearPedidoPorId(Long id) {
        return consultas.um(VENDAS + " WHERE id=? FOR UPDATE", Venda.class, id);
    }

    public Long inserirPedidoComercial(
            String cliente,
            String produto,
            Integer quantidade,
            BigDecimal valorUnitario,
            LocalDate entrega,
            LocalDate vencimento) {
        return jdbc.queryForObject(
                "INSERT INTO"
                    + " comercial_pedido(cliente,produto,quantidade,valor_unitario,entrega,vencimento,status)"
                    + " VALUES (?,?,?,?,?,?,'RASCUNHO') RETURNING id",
                Long.class,
                cliente,
                produto,
                quantidade,
                valorUnitario,
                entrega,
                vencimento);
    }

    public int atualizarPedidoComercial(
            String cliente,
            String produto,
            Integer quantidade,
            BigDecimal valorUnitario,
            LocalDate entrega,
            LocalDate vencimento,
            Long id) {
        return jdbc.update(
                "UPDATE comercial_pedido SET"
                    + " cliente=?,produto=?,quantidade=?,valor_unitario=?,entrega=?,vencimento=?,versao=versao+1"
                    + " WHERE id=?",
                cliente,
                produto,
                quantidade,
                valorUnitario,
                entrega,
                vencimento,
                id);
    }

    public Venda buscarPedidoPorId(Long registro) {
        return consultas.um(VENDAS + " WHERE id=?", Venda.class, registro);
    }

    public int atualizarPedidoComercialConfirmado(long id) {
        return jdbc.update(
                "UPDATE comercial_pedido SET status='CONFIRMADO',versao=versao+1 WHERE id=?", id);
    }

    public int atualizarPedidoComercialCancelado(long id) {
        return jdbc.update(
                "UPDATE comercial_pedido SET status='CANCELADO',versao=versao+1 WHERE id=?", id);
    }
}
