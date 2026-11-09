package com.aratech.moveis.compras.cotacoes.repository.imp;

import com.aratech.moveis.compras.cotacoes.dto.Cotacao;
import com.aratech.moveis.compras.cotacoes.repository.CotacaoRepository;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.repository.OperacaoConsultaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CotacaoRepositoryImp implements CotacaoRepository {
    private final JdbcTemplate jdbc;
    private final OperacaoConsultaRepository consultas;
    private static final String SELECT =
            "SELECT"
                + " c.id,c.pedido_id,p.item,p.quantidade,c.fornecedor1,c.valor1,c.fornecedor2,c.valor2,c.fornecedor3,c.valor3,c.escolhido,c.status,c.versao"
                + " FROM compra_cotacao c JOIN compra_pedido p ON p.id=c.pedido_id";

    public Pagina<Cotacao> listarCotacoes(int pagina, int tamanho, String busca) {
        return consultas.pagina(SELECT, Cotacao.class, pagina, tamanho, busca);
    }

    public List<Opcao> listarPedidosAbertos() {
        return jdbc.query(
                "SELECT id,'PC-'||id||' — '||item FROM compra_pedido WHERE status IN"
                        + " ('Rascunho','Aguardando Aprovação') ORDER BY id DESC",
                (r, n) -> new Opcao(r.getLong(1), r.getString(2)));
    }

    public List<Map<String, Object>> bloquearPedidoAberto(Long pedidoId) {
        return jdbc.queryForList(
                "SELECT id FROM compra_pedido WHERE id=? AND status IN ('Rascunho','Aguardando"
                        + " Aprovação') FOR UPDATE",
                pedidoId);
    }

    public Long inserirCotacao(
            Long pedidoId,
            String valor,
            BigDecimal valor2,
            String valor3,
            BigDecimal valor4,
            String valor5,
            BigDecimal valor6) {
        return jdbc.queryForObject(
                "INSERT INTO"
                    + " compra_cotacao(pedido_id,fornecedor1,valor1,fornecedor2,valor2,fornecedor3,valor3)"
                    + " VALUES (?,?,?,?,?,?,?) RETURNING id",
                Long.class,
                pedidoId,
                valor,
                valor2,
                valor3,
                valor4,
                valor5,
                valor6);
    }

    public Cotacao buscarCotacao(Long id) {
        return consultas.um(SELECT + " WHERE c.id=?", Cotacao.class, id);
    }

    public Cotacao bloquearCotacao(long id) {
        return consultas.um(SELECT + " WHERE c.id=? FOR UPDATE OF c", Cotacao.class, id);
    }

    public int definirFornecedorEPreco(String valor, BigDecimal valor2, Long pedidoId) {
        return jdbc.update(
                "UPDATE compra_pedido SET fornecedor=?,valor_unitario=?,versao=versao+1 WHERE id=?",
                valor,
                valor2,
                pedidoId);
    }

    public int registrarEscolha(int escolha, long id) {
        return jdbc.update(
                "UPDATE compra_cotacao SET escolhido=?,status='ESCOLHIDA',versao=versao+1 WHERE"
                        + " id=?",
                escolha,
                id);
    }

    public int atualizarCotacaoCancelada(long id) {
        return jdbc.update(
                "UPDATE compra_cotacao SET status='CANCELADA',versao=versao+1 WHERE id=?", id);
    }
}
