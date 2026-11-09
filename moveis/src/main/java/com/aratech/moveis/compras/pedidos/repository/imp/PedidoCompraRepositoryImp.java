package com.aratech.moveis.compras.pedidos.repository.imp;

import com.aratech.moveis.compras.pedidos.dto.Pedido;
import com.aratech.moveis.compras.pedidos.repository.PedidoCompraRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PedidoCompraRepositoryImp implements PedidoCompraRepository {
    private final JdbcTemplate jdbc;

    private Pedido map(ResultSet r, int n) throws SQLException {
        long id = r.getLong("id");
        var instante = r.getTimestamp("decidido_em");
        var entrega = r.getDate("data_entrega");
        var vencimento = r.getDate("data_vencimento");
        return new Pedido(
                id,
                "PC-" + String.format("%04d", id),
                r.getString("origem"),
                r.getString("tipo"),
                r.getObject("estoque_id", Long.class),
                r.getObject("solicitacao_id", Long.class),
                r.getString("item"),
                r.getString("codigo"),
                r.getBigDecimal("quantidade"),
                r.getString("unidade"),
                r.getString("fornecedor"),
                r.getBigDecimal("valor_unitario"),
                r.getDate("data_pedido").toLocalDate(),
                entrega == null ? null : entrega.toLocalDate(),
                r.getString("status"),
                r.getString("motivo"),
                r.getString("solicitante"),
                r.getString("decidido_nome"),
                instante == null ? null : instante.toInstant(),
                r.getLong("versao"),
                vencimento == null ? null : vencimento.toLocalDate());
    }

    public List<Pedido> listarTodos() {
        return jdbc.query("SELECT * FROM compra_pedido ORDER BY id DESC", this::map);
    }

    public List<Pedido> listarPorOrigem(String origem) {
        return jdbc.query(
                "SELECT * FROM compra_pedido WHERE origem=? ORDER BY id DESC", this::map, origem);
    }

    public List<Pedido> buscarPorId(boolean bloquear, Long id) {
        return jdbc.query(
                "SELECT * FROM compra_pedido WHERE id=?" + (bloquear ? " FOR UPDATE" : ""),
                this::map,
                id);
    }

    public List<Map<String, Object>> bloquearItemDeEstoque(boolean produto, Long id) {
        return jdbc.queryForList(
                "SELECT * FROM "
                        + (produto ? "produto" : "manutencao_peca_estoque")
                        + " WHERE id=? AND ativo IS TRUE FOR UPDATE",
                id);
    }

    public int definirQuantidadeMinima(boolean produto, BigDecimal valor, Long id) {
        return jdbc.update(
                "UPDATE "
                        + (produto ? "produto" : "manutencao_peca_estoque")
                        + " SET quantidade_minima=? WHERE id=?",
                valor,
                id);
    }

    public String buscarFornecedorDoProduto(Long id) {
        return jdbc.queryForObject(
                "SELECT f.nome FROM fornecedor f JOIN produto p ON p.fornecedor_id=f.id WHERE"
                        + " p.id=?",
                String.class,
                id);
    }

    public int inserirMonitoramento(String origem, Long id) {
        return jdbc.update(
                "INSERT INTO compra_monitoramento(origem,estoque_id) VALUES (?,?) ON CONFLICT DO"
                        + " NOTHING",
                origem,
                id);
    }

    public Boolean bloquearMonitoramento(String origem, Long id) {
        return jdbc.queryForObject(
                "SELECT abaixo FROM compra_monitoramento WHERE origem=? AND estoque_id=? FOR"
                        + " UPDATE",
                Boolean.class,
                origem,
                id);
    }

    public int atualizarMonitoramento(boolean baixo, String origem, Long id) {
        return jdbc.update(
                "UPDATE compra_monitoramento SET abaixo=? WHERE origem=? AND estoque_id=?",
                baixo,
                origem,
                id);
    }

    public int cancelarReposicaoPendente(String origem, Long id) {
        return jdbc.update(
                "UPDATE compra_pedido SET status='Cancelado',motivo='Reposição automática"
                        + " cancelada: saldo acima do mínimo ou monitoramento"
                        + " desativado.',versao=versao+1 WHERE origem=? AND estoque_id=? AND"
                        + " tipo='ESTOQUE_MINIMO' AND status='Aguardando Aprovação'",
                origem,
                id);
    }

    public int atualizarQuantidadeDaReposicao(
            BigDecimal quantidade, String origem, Long id, BigDecimal quantidade2) {
        return jdbc.update(
                "UPDATE compra_pedido SET quantidade=?,versao=versao+1 WHERE origem=? AND"
                    + " estoque_id=? AND tipo='ESTOQUE_MINIMO' AND status='Aguardando Aprovação'"
                    + " AND quantidade<>?",
                quantidade,
                origem,
                id,
                quantidade2);
    }

    public int inserirReposicaoSeNecessaria(
            String origem,
            Long id,
            String item,
            String codigo,
            BigDecimal quantidade,
            String unidade,
            String fornecedor,
            String origem2,
            Long id2) {
        return jdbc.update(
                "INSERT INTO"
                    + " compra_pedido(origem,tipo,estoque_id,item,codigo,quantidade,unidade,fornecedor,status,solicitante)"
                    + " SELECT ?,'ESTOQUE_MINIMO',?,?,?,?,?,?,'Aguardando Aprovação','Reposição"
                    + " automática' WHERE NOT EXISTS(SELECT 1 FROM compra_pedido WHERE origem=? AND"
                    + " estoque_id=? AND tipo='ESTOQUE_MINIMO' AND status IN ('Aguardando"
                    + " Aprovação','Aprovado'))",
                origem,
                id,
                item,
                codigo,
                quantidade,
                unidade,
                fornecedor,
                origem2,
                id2);
    }

    public int inserirPedidoDaSolicitacao(
            Long id,
            String nomePeca,
            String codigoPeca,
            BigDecimal quantidade,
            String unidade,
            String solicitante) {
        return jdbc.update(
                "INSERT INTO"
                    + " compra_pedido(origem,tipo,solicitacao_id,item,codigo,quantidade,unidade,status,solicitante)"
                    + " VALUES ('MANUTENCAO','SOLICITACAO_PECA',?,?,?,?,?,'Aguardando Aprovação',?)"
                    + " ON CONFLICT(solicitacao_id) DO NOTHING",
                id,
                nomePeca,
                codigoPeca,
                quantidade,
                unidade,
                solicitante);
    }

    public int registrarDecisao(String destino, Object valor, Long id, String nome, Long id2) {
        return jdbc.update(
                "UPDATE compra_pedido SET"
                    + " status=?,motivo=?,decidido_por=?,decidido_nome=?,decidido_em=CURRENT_TIMESTAMP,versao=versao+1"
                    + " WHERE id=?",
                destino,
                valor,
                id,
                nome,
                id2);
    }

    public Long inserirPedidoManual(
            String item,
            BigDecimal quantidade,
            String fornecedor,
            BigDecimal valorUnitario,
            LocalDate dataPedido,
            LocalDate dataEntrega,
            String nome) {
        return jdbc.queryForObject(
                "INSERT INTO"
                    + " compra_pedido(origem,tipo,item,quantidade,fornecedor,valor_unitario,data_pedido,data_entrega,status,solicitante)"
                    + " VALUES ('COMPRAS','MANUAL',?,?,?,?,?,?,'Rascunho',?) RETURNING id",
                Long.class,
                item,
                quantidade,
                fornecedor,
                valorUnitario,
                dataPedido,
                dataEntrega,
                nome);
    }

    public int atualizarPedidoManual(
            String item,
            BigDecimal quantidade,
            String fornecedor,
            BigDecimal valorUnitario,
            LocalDate dataPedido,
            LocalDate dataEntrega,
            Long id) {
        return jdbc.update(
                "UPDATE compra_pedido SET"
                    + " item=?,quantidade=?,fornecedor=?,valor_unitario=?,data_pedido=?,data_entrega=?,versao=versao+1"
                    + " WHERE id=?",
                item,
                quantidade,
                fornecedor,
                valorUnitario,
                dataPedido,
                dataEntrega,
                id);
    }

    public int definirVencimento(LocalDate dataVencimento, Long id) {
        return jdbc.update(
                "UPDATE compra_pedido SET data_vencimento=? WHERE id=?", dataVencimento, id);
    }

    public int atualizarPedidoAguardandoAprovação(Long id) {
        return jdbc.update(
                "UPDATE compra_pedido SET status='Aguardando Aprovação',versao=versao+1 WHERE id=?",
                id);
    }

    public int cancelarRascunho(Long id) {
        return jdbc.update(
                "UPDATE compra_pedido SET status='Cancelado',motivo='Rascunho removido pelo"
                        + " usuário.',versao=versao+1 WHERE id=?",
                id);
    }
}
