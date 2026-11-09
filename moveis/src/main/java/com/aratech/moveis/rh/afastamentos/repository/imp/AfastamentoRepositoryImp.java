package com.aratech.moveis.rh.afastamentos.repository.imp;

import com.aratech.moveis.rh.afastamentos.dto.Afastamento;
import com.aratech.moveis.rh.afastamentos.repository.AfastamentoRepository;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.repository.OperacaoConsultaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AfastamentoRepositoryImp implements AfastamentoRepository {
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

    public Pagina<Afastamento> listarAfastamentos(int pagina, int tamanho, String busca) {
        return consultas.pagina(AFASTAMENTOS, Afastamento.class, pagina, tamanho, busca);
    }

    public Long contarSobreposicoesAtivas(Long funcionarioId, LocalDate fim, LocalDate inicio) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM rh_afastamento WHERE funcionario_id=? AND status IN"
                        + " ('PENDENTE','APROVADO') AND inicio<=? AND fim>=?",
                Long.class,
                funcionarioId,
                fim,
                inicio);
    }

    public Long inserirAfastamento(
            Long funcionarioId, String tipo, LocalDate inicio, LocalDate fim, String valor) {
        return jdbc.queryForObject(
                "INSERT INTO rh_afastamento(funcionario_id,tipo,inicio,fim,motivo) VALUES"
                        + " (?,?,?,?,?) RETURNING id",
                Long.class,
                funcionarioId,
                tipo,
                inicio,
                fim,
                valor);
    }

    public Afastamento buscarAfastamento(Long id) {
        return consultas.um(AFASTAMENTOS + " WHERE a.id=?", Afastamento.class, id);
    }

    public Afastamento bloquearAfastamento(long id) {
        return consultas.um(AFASTAMENTOS + " WHERE a.id=? FOR UPDATE OF a", Afastamento.class, id);
    }

    public Long contarAprovadosSobrepostos(
            Long funcionarioId, long id, LocalDate fim, LocalDate inicio) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM rh_afastamento WHERE funcionario_id=? AND id<>? AND"
                        + " status='APROVADO' AND inicio<=? AND fim>=?",
                Long.class,
                funcionarioId,
                id,
                fim,
                inicio);
    }

    public int registrarDecisao(String destino, String valor, long id) {
        return jdbc.update(
                "UPDATE rh_afastamento SET status=?,decisao=?,versao=versao+1 WHERE id=?",
                destino,
                valor,
                id);
    }
}
