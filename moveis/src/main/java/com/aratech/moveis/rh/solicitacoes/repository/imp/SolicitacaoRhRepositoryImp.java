package com.aratech.moveis.rh.solicitacoes.repository.imp;

import com.aratech.moveis.rh.solicitacoes.dto.Solicitacao;
import com.aratech.moveis.rh.solicitacoes.repository.SolicitacaoRhRepository;
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
public class SolicitacaoRhRepositoryImp implements SolicitacaoRhRepository {
    private final JdbcTemplate jdbc;
    private final OperacaoConsultaRepository consultas;
    private static final String AFASTAMENTOS =
            "SELECT a.id,a.funcionario_id,f.nome_completo"
                + " funcionario,a.tipo,a.inicio,a.fim,a.motivo,CASE WHEN a.status='APROVADO' AND"
                + " CURRENT_DATE BETWEEN a.inicio AND a.fim THEN 'EM_CURSO' WHEN"
                + " a.status='APROVADO' AND a.fim<CURRENT_DATE THEN 'CONCLUIDO' ELSE a.status END"
                + " status,a.decisao,a.versao FROM rh_afastamento a JOIN funcionario f ON"
                + " f.id=a.funcionario_id";
    private static final String SOLICITACOES =
            "SELECT s.id,s.funcionario_id,f.nome_completo"
                + " funcionario,s.tipo,s.descricao,s.status,s.resposta,s.criado_em,s.versao FROM"
                + " rh_solicitacao s JOIN funcionario f ON f.id=s.funcionario_id";

    public List<Opcao> listarFuncionariosAtivos() {
        return jdbc.query(
                "SELECT id,nome_completo FROM funcionario WHERE ativo IS TRUE ORDER BY"
                        + " nome_completo",
                (r, n) -> new Opcao(r.getLong(1), r.getString(2)));
    }

    public List<Map<String, Object>> bloquearFuncionarioAtivo(long id) {
        return jdbc.queryForList(
                "SELECT id FROM funcionario WHERE id=? AND ativo IS TRUE FOR UPDATE", id);
    }

    public Pagina<Solicitacao> listarSolicitacoes(int pagina, int tamanho, String busca) {
        return consultas.pagina(SOLICITACOES, Solicitacao.class, pagina, tamanho, busca);
    }

    public Long inserirSolicitacao(Long funcionarioId, String tipo, String descricao) {
        return jdbc.queryForObject(
                "INSERT INTO rh_solicitacao(funcionario_id,tipo,descricao) VALUES (?,?,?) RETURNING"
                        + " id",
                Long.class,
                funcionarioId,
                tipo,
                descricao);
    }

    public Solicitacao buscarSolicitacao(Long id) {
        return consultas.um(SOLICITACOES + " WHERE s.id=?", Solicitacao.class, id);
    }

    public Solicitacao bloquearSolicitacao(long id) {
        return consultas.um(
                "SELECT s.id,s.funcionario_id,''"
                        + " funcionario,s.tipo,s.descricao,s.status,s.resposta,s.criado_em,s.versao"
                        + " FROM rh_solicitacao s WHERE s.id=? FOR UPDATE",
                Solicitacao.class,
                id);
    }

    public int registrarResposta(String destino, String valor, long id) {
        return jdbc.update(
                "UPDATE rh_solicitacao SET status=?,resposta=?,versao=versao+1 WHERE id=?",
                destino,
                valor,
                id);
    }
}
