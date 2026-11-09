package com.aratech.moveis.carregamento.expedicao.repository.imp;

import com.aratech.moveis.carregamento.expedicao.repository.ExpedicaoRepository;
import com.aratech.moveis.pcp.producao.dto.EstadoOrdem;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.operacao.repository.OperacaoConsultaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ExpedicaoRepositoryImp implements ExpedicaoRepository {
    private final JdbcTemplate jdbc;
    private final OperacaoConsultaRepository consultas;
    static final String ORDENS =
            "SELECT o.id,o.produto,o.quantidade,o.inicio,o.previsao,o.responsavel,o.linha_id,l.nome"
                + " linha,o.horas_previstas,o.pedido_id,o.status,o.concluido_em,o.expedido_em,o.versao"
                + " FROM pcp_ordem o LEFT JOIN pcp_linha l ON l.id=o.linha_id";
    static final String VENDAS =
            "SELECT id,cliente,produto,quantidade,valor_unitario,entrega,vencimento,status,versao"
                    + " FROM comercial_pedido";

    public EstadoOrdem bloquearOrdemParaExpedicao(long id) {
        return consultas.um(
                "SELECT id,versao,status,pedido_id FROM pcp_ordem WHERE id=? FOR UPDATE",
                EstadoOrdem.class,
                id);
    }

    public List<Opcao> listarCaminhoesEmAtendimento() {
        return jdbc.query(
                "SELECT id,placa||' — '||empresa nome FROM registro_chegada WHERE"
                        + " setor_responsavel='CARREGAMENTO' AND tipo_operacao='CARREGAR' AND"
                        + " status='EM_RECEBIMENTO' ORDER BY data_chegada",
                (r, n) -> new Opcao(r.getLong(1), r.getString(2)));
    }

    public List<Map<String, Object>> bloquearCaminhaoEmAtendimento(Long referenciaId) {
        return jdbc.queryForList(
                "SELECT id FROM registro_chegada WHERE id=? AND setor_responsavel='CARREGAMENTO'"
                        + " AND tipo_operacao='CARREGAR' AND status='EM_RECEBIMENTO' FOR UPDATE",
                referenciaId);
    }

    public int registrarExpedicao(Long referenciaId, long id) {
        return jdbc.update(
                "UPDATE pcp_ordem SET expedido_em=CURRENT_TIMESTAMP,chegada_id=?,versao=versao+1"
                        + " WHERE id=?",
                referenciaId,
                id);
    }

    public int atualizarPedidoComercialExpedido(Long pedidoId) {
        return jdbc.update(
                "UPDATE comercial_pedido SET status='EXPEDIDO',versao=versao+1 WHERE id=?",
                pedidoId);
    }
}
