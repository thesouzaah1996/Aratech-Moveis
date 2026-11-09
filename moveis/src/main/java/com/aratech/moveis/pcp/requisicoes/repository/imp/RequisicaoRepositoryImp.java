package com.aratech.moveis.pcp.requisicoes.repository.imp;

import com.aratech.moveis.pcp.requisicoes.dto.Requisicao;
import com.aratech.moveis.pcp.requisicoes.repository.RequisicaoRepository;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.repository.OperacaoConsultaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RequisicaoRepositoryImp implements RequisicaoRepository {
    private final JdbcTemplate jdbc;
    private final OperacaoConsultaRepository consultas;
    private static final String SELECT =
            "SELECT r.id,r.ordem_id,o.produto produto_final,r.produto_id,p.nome"
                + " material,r.quantidade,r.motivo,r.status,r.resposta,r.versao FROM pcp_requisicao"
                + " r JOIN pcp_ordem o ON o.id=r.ordem_id JOIN produto p ON p.id=r.produto_id";

    public Pagina<Requisicao> listarRequisicoes(int pagina, int tamanho, String busca) {
        return consultas.pagina(SELECT, Requisicao.class, pagina, tamanho, busca);
    }

    public List<Opcao> listarOrdensAbertas() {
        return jdbc.query(
                "SELECT id,'OP-'||id||' — '||produto FROM pcp_ordem WHERE status IN"
                        + " ('PLANEJADA','EM_PRODUCAO') ORDER BY id DESC",
                (r, n) -> new Opcao(r.getLong(1), r.getString(2)));
    }

    public List<Opcao> listarMateriaisDisponiveis() {
        return jdbc.query(
                "SELECT id,sku||' — '||nome||' (saldo: '||quantidade||')' FROM produto WHERE ativo"
                        + " IS TRUE ORDER BY nome",
                (r, n) -> new Opcao(r.getLong(1), r.getString(2)));
    }

    public List<Map<String, Object>> bloquearOrdemAberta(Long ordemId) {
        return jdbc.queryForList(
                "SELECT id FROM pcp_ordem WHERE id=? AND status IN ('PLANEJADA','EM_PRODUCAO') FOR"
                        + " UPDATE",
                ordemId);
    }

    public List<Map<String, Object>> buscarMaterialAtivo(Long produtoId) {
        return jdbc.queryForList("SELECT id FROM produto WHERE id=? AND ativo IS TRUE", produtoId);
    }

    public Long inserirRequisicao(Long ordemId, Long produtoId, Integer quantidade, String valor) {
        return jdbc.queryForObject(
                "INSERT INTO pcp_requisicao(ordem_id,produto_id,quantidade,motivo) VALUES (?,?,?,?)"
                        + " RETURNING id",
                Long.class,
                ordemId,
                produtoId,
                quantidade,
                valor);
    }

    public Requisicao buscarRequisicao(Long id) {
        return consultas.um(SELECT + " WHERE r.id=?", Requisicao.class, id);
    }

    public Requisicao bloquearRequisicao(long id) {
        return consultas.um(SELECT + " WHERE r.id=? FOR UPDATE OF r", Requisicao.class, id);
    }

    public List<Map<String, Object>> bloquearOrdemPlanejada(Long ordemId) {
        return jdbc.queryForList(
                "SELECT id FROM pcp_ordem WHERE id=? AND status='PLANEJADA' FOR UPDATE", ordemId);
    }

    public int registrarDecisao(String destino, String motivo, long id) {
        return jdbc.update(
                "UPDATE pcp_requisicao SET status=?,resposta=?,versao=versao+1 WHERE id=?",
                destino,
                motivo,
                id);
    }
}
