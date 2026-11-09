package com.aratech.moveis.pcp.producao.repository.imp;

import com.aratech.moveis.pcp.producao.dto.Carga;
import com.aratech.moveis.pcp.producao.dto.EstadoOrdem;
import com.aratech.moveis.pcp.producao.dto.Linha;
import com.aratech.moveis.pcp.producao.dto.Ordem;
import com.aratech.moveis.pcp.producao.model.ReservaCapacidade;
import com.aratech.moveis.pcp.producao.repository.ProducaoRepository;
import com.aratech.moveis.shared.dto.Opcao;
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
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProducaoRepositoryImp implements ProducaoRepository {
    private final JdbcTemplate jdbc;
    private final OperacaoConsultaRepository consultas;
    static final String ORDENS =
            "SELECT o.id,o.produto,o.quantidade,o.inicio,o.previsao,o.responsavel,o.linha_id,l.nome"
                + " linha,o.horas_previstas,o.pedido_id,o.status,o.concluido_em,o.expedido_em,o.versao"
                + " FROM pcp_ordem o LEFT JOIN pcp_linha l ON l.id=o.linha_id";
    static final String VENDAS =
            "SELECT id,cliente,produto,quantidade,valor_unitario,entrega,vencimento,status,versao"
                    + " FROM comercial_pedido";

    public Pagina<Linha> listarLinhas(int pagina, int tamanho, String busca) {
        return consultas.pagina("SELECT * FROM pcp_linha", Linha.class, pagina, tamanho, busca);
    }

    public Pagina<Ordem> listarOrdens(String filtro, int pagina, int tamanho, String busca) {
        return consultas.pagina(
                ORDENS
                        + switch (filtro) {
                            case "expedicao" ->
                                    " WHERE o.status='FINALIZADA' AND o.expedido_em IS NULL";
                            case "lotes" -> " WHERE o.status='FINALIZADA'";
                            case "programacao" -> " WHERE o.status IN ('PLANEJADA','EM_PRODUCAO')";
                            default -> "";
                        },
                Ordem.class,
                pagina,
                tamanho,
                busca);
    }

    public List<Opcao> listarLinhasAtivas() {
        return jdbc.query(
                "SELECT id,nome FROM pcp_linha WHERE ativo ORDER BY nome",
                (r, n) -> new Opcao(r.getLong(1), r.getString(2)));
    }

    public Ordem buscarOrdem(long id) {
        return consultas.um(ORDENS + " WHERE o.id=?", Ordem.class, id);
    }

    public Long inserirLinha(String nome, BigDecimal horasDia) {
        return jdbc.queryForObject(
                "INSERT INTO pcp_linha(nome,horas_dia) VALUES (?,?) RETURNING id",
                Long.class,
                nome,
                horasDia);
    }

    public Linha bloquearLinhaPorId(Long registro) {
        return consultas.um("SELECT * FROM pcp_linha WHERE id=? FOR UPDATE", Linha.class, registro);
    }

    public Long contarOrdensAbertasDaLinha(Long registro) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM pcp_ordem WHERE linha_id=? AND status IN"
                        + " ('PLANEJADA','EM_PRODUCAO')",
                Long.class,
                registro);
    }

    public int atualizarLinha(String nome, BigDecimal horasDia, Long registro) {
        return jdbc.update(
                "UPDATE pcp_linha SET nome=?,horas_dia=?,versao=versao+1 WHERE id=?",
                nome,
                horasDia,
                registro);
    }

    public Linha buscarLinha(Long registro) {
        return consultas.um("SELECT * FROM pcp_linha WHERE id=?", Linha.class, registro);
    }

    public List<ReservaCapacidade> listarReservasDeCapacidade(
            Long linhaId, Long ignorar, Long ignorar2, LocalDate previsao, LocalDate inicio) {
        return jdbc.query(
                "SELECT inicio,previsao,horas_previstas FROM pcp_ordem WHERE linha_id=? AND status"
                        + " IN ('PLANEJADA','EM_PRODUCAO') AND (?::bigint IS NULL OR id<>?) AND"
                        + " inicio<=? AND previsao>=?",
                new org.springframework.jdbc.core.DataClassRowMapper<>(ReservaCapacidade.class),
                linhaId,
                ignorar,
                ignorar2,
                previsao,
                inicio);
    }

    public EstadoOrdem bloquearOrdemPorId(Long id) {
        return consultas.um(
                "SELECT id,versao,status,pedido_id FROM pcp_ordem WHERE id=? FOR UPDATE",
                EstadoOrdem.class,
                id);
    }

    public Long inserirOrdem(
            String produto,
            Integer quantidade,
            LocalDate inicio,
            LocalDate previsao,
            String responsavel,
            Long linhaId,
            BigDecimal horasPrevistas) {
        return jdbc.queryForObject(
                "INSERT INTO"
                    + " pcp_ordem(produto,quantidade,inicio,previsao,responsavel,linha_id,horas_previstas,status)"
                    + " VALUES (?,?,?,?,?,?,?,'PLANEJADA') RETURNING id",
                Long.class,
                produto,
                quantidade,
                inicio,
                previsao,
                responsavel,
                linhaId,
                horasPrevistas);
    }

    public int atualizarOrdem(
            String produto,
            Integer quantidade,
            LocalDate inicio,
            LocalDate previsao,
            String responsavel,
            Long linhaId,
            BigDecimal horasPrevistas,
            Long registro) {
        return jdbc.update(
                "UPDATE pcp_ordem SET"
                    + " produto=?,quantidade=?,inicio=?,previsao=?,responsavel=?,linha_id=?,horas_previstas=?,versao=versao+1"
                    + " WHERE id=?",
                produto,
                quantidade,
                inicio,
                previsao,
                responsavel,
                linhaId,
                horasPrevistas,
                registro);
    }

    public Long contarRequisicoesPendentes(long id) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM pcp_requisicao WHERE ordem_id=? AND status='PENDENTE'",
                Long.class,
                id);
    }

    public Long contarRequisicoesAtendidas(long id) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM pcp_requisicao WHERE ordem_id=? AND status='ATENDIDA'",
                Long.class,
                id);
    }

    public int atualizarRequisicaoRecusada(long id) {
        return jdbc.update(
                "UPDATE pcp_requisicao SET status='RECUSADA',resposta='Ordem cancelada pelo"
                        + " PCP',versao=versao+1 WHERE ordem_id=? AND status='PENDENTE'",
                id);
    }

    public int atualizarOrdemEmProducao(long id) {
        return jdbc.update(
                "UPDATE pcp_ordem SET"
                    + " status='EM_PRODUCAO',iniciado_em=CURRENT_TIMESTAMP,versao=versao+1 WHERE"
                    + " id=?",
                id);
    }

    public int atualizarPedidoComercialEmProducao(Long pedidoId) {
        return jdbc.update(
                "UPDATE comercial_pedido SET status='EM_PRODUCAO',versao=versao+1 WHERE id=?",
                pedidoId);
    }

    public int atualizarOrdemFinalizada(long id) {
        return jdbc.update(
                "UPDATE pcp_ordem SET"
                    + " status='FINALIZADA',concluido_em=CURRENT_TIMESTAMP,versao=versao+1 WHERE"
                    + " id=?",
                id);
    }

    public int atualizarPedidoComercialPronto(Long pedidoId) {
        return jdbc.update(
                "UPDATE comercial_pedido SET status='PRONTO',versao=versao+1 WHERE id=?", pedidoId);
    }

    public int atualizarOrdemCancelada(long id) {
        return jdbc.update(
                "UPDATE pcp_ordem SET status='CANCELADA',versao=versao+1 WHERE id=?", id);
    }

    public List<Carga> consultarCargaDasLinhas() {
        return jdbc.query(
                "SELECT"
                    + " l.id,l.nome,l.horas_dia,COALESCE(sum(o.horas_previstas/(o.previsao-o.inicio+1)),0)"
                    + " carga_hoje,l.horas_dia-COALESCE(sum(o.horas_previstas/(o.previsao-o.inicio+1)),0)"
                    + " saldo_hoje FROM pcp_linha l LEFT JOIN pcp_ordem o ON o.linha_id=l.id AND"
                    + " o.status IN ('PLANEJADA','EM_PRODUCAO') AND CURRENT_DATE BETWEEN o.inicio"
                    + " AND o.previsao WHERE l.ativo GROUP BY l.id ORDER BY l.nome",
                new org.springframework.jdbc.core.DataClassRowMapper<>(Carga.class));
    }

    public Map<String, Object> consultarIndicadoresDeProducao() {
        return jdbc.queryForMap(
                "SELECT count(*) FILTER(WHERE status='PLANEJADA') planejadas,count(*) FILTER(WHERE"
                    + " status='EM_PRODUCAO') andamento,count(*) FILTER(WHERE status IN"
                    + " ('PLANEJADA','EM_PRODUCAO') AND previsao<CURRENT_DATE)"
                    + " atrasadas,COALESCE(sum(quantidade) FILTER(WHERE status='FINALIZADA' AND"
                    + " concluido_em>=date_trunc('month',CURRENT_TIMESTAMP)),0) produzidas,count(*)"
                    + " FILTER(WHERE status='FINALIZADA' AND expedido_em IS NULL) prontas FROM"
                    + " pcp_ordem");
    }

    public int inserirOrdemDaEncomenda(String produto, int quantidade, LocalDate entrega, long id) {
        return jdbc.update(
                "INSERT INTO"
                    + " pcp_ordem(produto,quantidade,inicio,previsao,responsavel,horas_previstas,pedido_id,status)"
                    + " VALUES (?,?,CURRENT_DATE,?,'A definir pelo PCP',1,?,'PLANEJADA')",
                produto,
                quantidade,
                entrega,
                id);
    }

    public List<String> bloquearOrdensDaEncomenda(long id) {
        return jdbc.queryForList(
                "SELECT status FROM pcp_ordem WHERE pedido_id=? FOR UPDATE", String.class, id);
    }

    public Long contarMateriaisEntreguesDaEncomenda(long id) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM pcp_requisicao r JOIN pcp_ordem o ON o.id=r.ordem_id WHERE"
                        + " o.pedido_id=? AND r.status='ATENDIDA'",
                Long.class,
                id);
    }

    public int cancelarOrdensDaEncomenda(long id) {
        return jdbc.update(
                "UPDATE pcp_ordem SET status='CANCELADA',versao=versao+1 WHERE pedido_id=?", id);
    }

    public int recusarPendentesDaEncomenda(long id) {
        return jdbc.update(
                "UPDATE pcp_requisicao SET status='RECUSADA',resposta='Encomenda cancelada pelo"
                        + " Comercial',versao=versao+1 WHERE ordem_id IN (SELECT id FROM pcp_ordem"
                        + " WHERE pedido_id=?) AND status='PENDENTE'",
                id);
    }
}
