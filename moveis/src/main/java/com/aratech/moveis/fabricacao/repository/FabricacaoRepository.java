package com.aratech.moveis.fabricacao.repository;

import com.aratech.moveis.fabricacao.dto.*;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Pagina;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;
import java.sql.*;
import java.time.*;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class FabricacaoRepository {
    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    public Pagina<ModeloResumo> modelos(int pagina, int tamanho, String busca, boolean ativos) {
        String filtro=" WHERE (?=FALSE OR m.ativo=TRUE) AND (strpos(lower(m.codigo),lower(?))>0 OR strpos(lower(m.nome),lower(?))>0)";
        Long total=jdbc.queryForObject("SELECT count(*) FROM fabricacao_modelo m"+filtro,Long.class,ativos,busca,busca);
        var itens=jdbc.query("SELECT m.id,m.codigo,m.nome,m.ativo,m.versao,jsonb_array_length(m.ficha->'itens') itens,(SELECT min(f.id) FROM fabricacao_foto f WHERE f.modelo_id=m.id AND f.ativo AND f.item_chave IS NULL) capa FROM fabricacao_modelo m"+filtro+" ORDER BY lower(m.nome),m.id LIMIT ? OFFSET ?",
                (r,n)->new ModeloResumo(r.getLong("id"),r.getString("codigo"),r.getString("nome"),r.getBoolean("ativo"),r.getLong("versao"),r.getInt("itens"),(Long)r.getObject("capa")),ativos,busca,busca,tamanho,(long)pagina*tamanho);
        return new Pagina<>(itens,total,pagina,tamanho);
    }
    public ModeloGuardaRoupa modelo(long id, boolean bloquear) {
        var modelos=jdbc.query("SELECT id,versao,ficha FROM fabricacao_modelo WHERE id=?"+(bloquear?" FOR UPDATE":""),
                (r,n)->new ModeloGuardaRoupa(r.getLong(1),r.getLong(2),json.readValue(r.getString(3),FichaGuardaRoupa.class),List.of()),id);
        if(modelos.isEmpty()) throw new AuthException(404,"Guarda-roupa não encontrado.");
        var m=modelos.getFirst();
        var chaves=m.ficha().itens().stream().map(ItemGuardaRoupa::chave).collect(java.util.stream.Collectors.toSet());
        var fotos=jdbc.query("SELECT id,item_chave,legenda FROM fabricacao_foto WHERE modelo_id=? AND ativo ORDER BY id",
                (r,n)->new FotoFabricacao(r.getLong(1),r.getObject(2,UUID.class),r.getString(3)),id).stream().filter(f->f.itemChave()==null||chaves.contains(f.itemChave())).toList();
        return new ModeloGuardaRoupa(m.id(),m.versao(),m.ficha(),fotos);
    }
    public long criar(FichaGuardaRoupa ficha) {
        return jdbc.queryForObject("INSERT INTO fabricacao_modelo(codigo,nome,ativo,ficha) VALUES (?,?,?,?::jsonb) RETURNING id",Long.class,ficha.codigo().strip(),ficha.nome().strip(),ficha.ativo(),json.writeValueAsString(ficha));
    }
    public void atualizar(long id,FichaGuardaRoupa ficha) {
        jdbc.update("UPDATE fabricacao_modelo SET codigo=?,nome=?,ativo=?,ficha=?::jsonb,versao=versao+1,atualizado_em=CURRENT_TIMESTAMP WHERE id=?",ficha.codigo().strip(),ficha.nome().strip(),ficha.ativo(),json.writeValueAsString(ficha),id);
    }
    public void incrementarVersao(long id) {
        jdbc.update("UPDATE fabricacao_modelo SET versao=versao+1,atualizado_em=CURRENT_TIMESTAMP WHERE id=?",id);
    }
    public long inserirFoto(long modelo,UUID item,String legenda,String mime,byte[] conteudo) {
        return jdbc.queryForObject("INSERT INTO fabricacao_foto(modelo_id,item_chave,legenda,mime,conteudo) VALUES (?,?,?,?,?) RETURNING id",Long.class,modelo,item,legenda,mime,conteudo);
    }
    public void retirarFoto(long modelo,long foto) {
        // Referências nas fichas congeladas dos lotes continuam disponíveis.
        if(jdbc.update("UPDATE fabricacao_foto SET ativo=FALSE WHERE modelo_id=? AND id=? AND ativo",modelo,foto)!=1)
            throw new AuthException(404,"Foto não encontrada.");
    }
    public record FotoConteudo(String mime,byte[] conteudo) {}
    public FotoConteudo foto(long modelo,long foto) {
        var fotos=jdbc.query("SELECT mime,conteudo FROM fabricacao_foto WHERE modelo_id=? AND id=?",(r,n)->new FotoConteudo(r.getString(1),r.getBytes(2)),modelo,foto);
        if(fotos.isEmpty())throw new AuthException(404,"Foto não encontrada.");
        return fotos.getFirst();
    }
    public long criarLote(NovoLoteCorte dto,ModeloGuardaRoupa modelo,long autorId,String autor) {
        return jdbc.queryForObject("INSERT INTO fabricacao_lote(modelo_id,modelo_versao,quantidade,previsao,observacoes,ficha,chave_operacao,autor_id,autor_nome) VALUES (?,?,?,?,?,?::jsonb,?,?,?) RETURNING id",Long.class,dto.modeloId(),modelo.versao(),dto.quantidade(),dto.previsao(),Objects.toString(dto.observacoes(),""),json.writeValueAsString(modelo),dto.chaveOperacao(),autorId,autor);
    }
    public List<Long> lotePorChave(UUID chave) {
        return jdbc.queryForList("SELECT id FROM fabricacao_lote WHERE chave_operacao=?",Long.class,chave);
    }
    public LoteCorte lote(long id,boolean bloquear) {
        var lotes=jdbc.query("SELECT * FROM fabricacao_lote WHERE id=?"+(bloquear?" FOR UPDATE":""),(r,n)->new LoteCorte(r.getLong("id"),r.getLong("modelo_id"),r.getLong("modelo_versao"),r.getInt("quantidade"),r.getDate("previsao").toLocalDate(),r.getString("observacoes"),r.getString("status"),r.getLong("versao"),json.readValue(r.getString("ficha"),ModeloGuardaRoupa.class),r.getString("autor_nome"),instante(r,"criado_em"),instante(r,"enviado_em"),instante(r,"iniciado_em"),instante(r,"concluido_em")),id);
        if(lotes.isEmpty())throw new AuthException(404,"Lote não encontrado.");
        return lotes.getFirst();
    }
    private Instant instante(ResultSet r,String coluna) throws SQLException {
        var valor=r.getTimestamp(coluna);return valor==null?null:valor.toInstant();
    }
    public Pagina<LoteResumo> lotes(int pagina,int tamanho,String busca,boolean corte,String status) {
        String filtro=" WHERE (?=FALSE OR status IN ('ENVIADO_CORTE','EM_CORTE','CORTADO')) AND (?='' OR status=?) AND (strpos(lower(ficha->'ficha'->>'nome'),lower(?))>0 OR strpos(lower(ficha->'ficha'->>'codigo'),lower(?))>0 OR CAST(id AS TEXT)=?)";
        Long total=jdbc.queryForObject("SELECT count(*) FROM fabricacao_lote"+filtro,Long.class,corte,status,status,busca,busca,busca);
        var itens=jdbc.query("SELECT id,ficha->'ficha'->>'codigo' codigo,ficha->'ficha'->>'nome' nome,quantidade,previsao,status,versao FROM fabricacao_lote"+filtro+" ORDER BY CASE status WHEN 'EM_CORTE' THEN 0 WHEN 'ENVIADO_CORTE' THEN 1 WHEN 'PLANEJADO' THEN 2 ELSE 3 END,previsao,id LIMIT ? OFFSET ?",(r,n)->new LoteResumo(r.getLong("id"),r.getString("codigo"),r.getString("nome"),r.getInt("quantidade"),r.getDate("previsao").toLocalDate(),r.getString("status"),r.getLong("versao")),corte,status,status,busca,busca,busca,tamanho,(long)pagina*tamanho);
        return new Pagina<>(itens,total,pagina,tamanho);
    }
    public void status(long id,String status) {
        String coluna=switch(status){case "ENVIADO_CORTE"->",enviado_em=CURRENT_TIMESTAMP";case "EM_CORTE"->",iniciado_em=CURRENT_TIMESTAMP";case "CORTADO"->",concluido_em=CURRENT_TIMESTAMP";default->"";};
        jdbc.update("UPDATE fabricacao_lote SET status=?,versao=versao+1"+coluna+" WHERE id=?",status,id);
    }
}
