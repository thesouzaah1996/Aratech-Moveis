package com.aratech.moveis.administracao.gestao.repository.imp;

import com.aratech.moveis.administracao.gestao.dto.Auditoria;
import com.aratech.moveis.administracao.gestao.dto.Parametro;
import com.aratech.moveis.administracao.gestao.repository.AdministracaoRepository;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.repository.OperacaoConsultaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdministracaoRepositoryImp implements AdministracaoRepository {
    private final JdbcTemplate jdbc;
    private final OperacaoConsultaRepository consultas;

    public Pagina<Auditoria> listarAuditoria(int pagina, int tamanho, String busca) {
        return consultas.pagina(
                "SELECT id,modulo,acao,entidade,registro_id,usuario_id,usuario,detalhe,criado_em"
                        + " FROM gestao_auditoria",
                Auditoria.class,
                pagina,
                tamanho,
                busca);
    }

    public List<Parametro> listarParametros() {
        return jdbc.query(
                "SELECT * FROM gestao_parametro ORDER BY chave",
                new org.springframework.jdbc.core.DataClassRowMapper<>(Parametro.class));
    }

    public Parametro bloquearParametro(String chave) {
        return consultas.um(
                "SELECT * FROM gestao_parametro WHERE chave=? FOR UPDATE", Parametro.class, chave);
    }

    public int atualizarParametro(Integer valor, String chave) {
        return jdbc.update(
                "UPDATE gestao_parametro SET valor=?,versao=versao+1 WHERE chave=?", valor, chave);
    }

    public Parametro buscarParametro(String chave) {
        return consultas.um("SELECT * FROM gestao_parametro WHERE chave=?", Parametro.class, chave);
    }

    public BigDecimal comprasPendentes() {
        return jdbc.queryForObject(
                "SELECT count(*) FROM compra_pedido WHERE status='Aguardando Aprovação'",
                BigDecimal.class);
    }

    public BigDecimal comprasSemProvisao() {
        return jdbc.queryForObject(
                "SELECT count(*) FROM compra_pedido p WHERE status='Recebido' AND NOT EXISTS(SELECT"
                    + " 1 FROM financeiro_titulo f WHERE f.origem='COMPRA' AND f.origem_id=p.id)",
                BigDecimal.class);
    }

    public BigDecimal ordensAtrasadas() {
        return jdbc.queryForObject(
                "SELECT count(*) FROM pcp_ordem WHERE status IN ('PLANEJADA','EM_PRODUCAO') AND"
                        + " previsao<CURRENT_DATE",
                BigDecimal.class);
    }

    public BigDecimal producaoProxima(int antecedencia) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM pcp_ordem WHERE status IN ('PLANEJADA','EM_PRODUCAO') AND"
                        + " previsao BETWEEN CURRENT_DATE AND CURRENT_DATE+?",
                BigDecimal.class,
                antecedencia);
    }

    public BigDecimal titulosVencidos() {
        return jdbc.queryForObject(
                "SELECT COALESCE(sum(valor),0) FROM financeiro_titulo WHERE status='PENDENTE' AND"
                        + " vencimento<CURRENT_DATE",
                BigDecimal.class);
    }

    public BigDecimal pagamentosProximos(int antecedencia) {
        return jdbc.queryForObject(
                "SELECT COALESCE(sum(valor),0) FROM financeiro_titulo WHERE tipo='PAGAR' AND"
                    + " status='PENDENTE' AND vencimento BETWEEN CURRENT_DATE AND CURRENT_DATE+?",
                BigDecimal.class,
                antecedencia);
    }

    public BigDecimal afastamentosPendentes() {
        return jdbc.queryForObject(
                "SELECT count(*) FROM rh_afastamento WHERE status='PENDENTE'", BigDecimal.class);
    }
}
