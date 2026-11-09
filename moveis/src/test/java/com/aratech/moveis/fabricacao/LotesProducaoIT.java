package com.aratech.moveis.fabricacao;

import com.aratech.moveis.fabricacao.dto.*;
import com.aratech.moveis.pcp.lotes.ProducaoDtos.*;
import java.util.concurrent.*;
import com.aratech.moveis.security.authorization.SessaoAuthorities;
import com.aratech.moveis.security.service.ContasService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class LotesProducaoIT {
    @Autowired jakarta.persistence.EntityManager em;
    @Autowired MockMvc mvc;@Autowired JdbcTemplate jdbc;@Autowired JsonMapper json;@Autowired ContasService contas;
    MockHttpSession admin,pcp,corte;
    @BeforeEach void preparar(){admin=sessao("ADMINISTRACAO_GUARDA_ROUPAS.CONSULTAR","ADMINISTRACAO_GUARDA_ROUPAS.CADASTRAR","ADMINISTRACAO_GUARDA_ROUPAS.ALTERAR");pcp=sessao("PCP_LOTES.CONSULTAR","PCP_LOTES.CADASTRAR","PCP_LOTES.ALTERAR");corte=sessao("CORTE_LOTES.CONSULTAR","CORTE_LOTES.ALTERAR","CORTE_LOTES.FINALIZAR");}
    MockHttpSession sessao(String...permissoes){
        long id=jdbc.queryForObject("INSERT INTO funcionario(nome_completo,email,ativo) VALUES ('Operador fabricação',?,TRUE) RETURNING id",Long.class,UUID.randomUUID()+"@aratechmoveis");
        jdbc.update("INSERT INTO acesso_conta(funcionario_id,senha_hash) VALUES (?,?)",id,"nao-utilizada-neste-teste");
        long perfil=jdbc.queryForObject("INSERT INTO perfil_acesso(nome) VALUES (?) RETURNING id",Long.class,"FAB-"+UUID.randomUUID());
        jdbc.update("INSERT INTO funcionario_perfil_acesso(funcionario_id,perfil_id) VALUES (?,?)",id,perfil);
        for(String p:permissoes)jdbc.update("INSERT INTO perfil_permissao_acesso(perfil_id,permissao) VALUES (?,?)",perfil,p);
        var u=contas.carregar(id,Instant.now());var contexto=SecurityContextHolder.createEmptyContext();contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(u,null,SessaoAuthorities.authorities(u)));
        var sessao=new MockHttpSession();sessao.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,contexto);return sessao;
    }
    String csrf(MockHttpSession sessao) throws Exception{return json.readTree(mvc.perform(get("/aratech/auth/csrf").session(sessao)).andReturn().getResponse().getContentAsString()).get("token").asText();}
    JsonNode enviar(MockHttpServletRequestBuilder req,MockHttpSession sessao,Object corpo) throws Exception{return json.readTree(mvc.perform(req.session(sessao).header("X-XSRF-TOKEN",csrf(sessao)).contentType("application/json").content(json.writeValueAsString(corpo))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));}
    long setor(String codigo){return jdbc.queryForObject("SELECT id FROM fabricacao_setor WHERE codigo=?",Long.class,codigo);}
    long produto(){
        long categoria=jdbc.queryForObject("INSERT INTO categoria(nome,nome_normalizado,ativo) VALUES (?,gen_random_uuid()::text,TRUE) RETURNING id",Long.class,"Categoria "+UUID.randomUUID());
        long fornecedor=jdbc.queryForObject("INSERT INTO fornecedor(nome,nome_normalizado,email,telefone,nome_representante,telefone_representante,email_representante,ativo) VALUES (?,gen_random_uuid()::text,gen_random_uuid()::text||'@teste.com','11999999999','Representante','11999999999','rep@teste.com',TRUE) RETURNING id",Long.class,"Fornecedor "+UUID.randomUUID());
        return jdbc.queryForObject("INSERT INTO produto(nome,sku,quantidade,categoria_id,fornecedor_id,local_armazenamento,ativo,criado_em) VALUES ('Dobradiça',?,2000,?,?,'A1',TRUE,CURRENT_TIMESTAMP) RETURNING id",Long.class,"TEST-"+UUID.randomUUID().toString().toUpperCase(Locale.ROOT),categoria,fornecedor);
    }
    Variante prepararVariante(long estoque) throws Exception {
        UUID lat=UUID.randomUUID(),dob=UUID.randomUUID();
        var lateral=FabricacaoTest.painel(lat,"LAT",new BigDecimal("2200"));
        var ferragem=new ItemGuardaRoupa(dob,"DOB","Dobradiça",ItemGuardaRoupa.Tipo.FERRAGEM,"Ferragens","Aço","Zincado",ItemGuardaRoupa.Unidade.UN,new BigDecimal("8"),false,null,null,null,ItemGuardaRoupa.Veio.SEM,Set.of(),"","","",Map.of());
        var ficha=FabricacaoTest.ficha("MODELO-"+UUID.randomUUID(),"Modelo X",0,lateral,ferragem);
        long modelo=enviar(post("/aratech/administracao/guarda-roupas"),admin,ficha).get("id").asLong();
        var dados=new FichaVariante(modelo,"BRANCO","Branco",true,0,ficha,List.of(new Roteiro(lat,setor("CORTE"),null,List.of(new Operacao(setor("CORTE"),"Cortar lateral","Corte branco"),new Operacao(setor("FURACAO"),"Furar lateral","Furos exclusivos"))),new Roteiro(dob,setor("ALMOXARIFADO"),estoque,List.of())));
        return json.treeToValue(enviar(post("/aratech/administracao/guarda-roupas/variantes"),admin,dados),Variante.class);
    }
    JsonNode planejar(Variante v) throws Exception {return enviar(post("/aratech/pcp/lotes/producao"),pcp,new NovoLote(v.id(),null,150,LocalDate.now().plusDays(2),"Aceite",UUID.randomUUID()));}
    @Test void calculaLiberaParcialmenteCongelaRevisaoESeparaInstrucoes() throws Exception {
        long produto=produto();var variante=prepararVariante(produto);var l=planejar(variante);long id=l.get("id").asLong();
        assertEquals(3,l.at("/previa/setores").size());assertEquals(0,l.at("/previa/pendencias").size());
        var grupos=json.treeToValue(l.get("previa"),Calculo.class);
        assertEquals(0,new BigDecimal("1200").compareTo(grupos.setores().stream().filter(g->g.setor().codigo().equals("ALMOXARIFADO")).findFirst().orElseThrow().linhas().getFirst().total()));
        assertEquals(0,new BigDecimal("300").compareTo(grupos.setores().stream().filter(g->g.setor().codigo().equals("CORTE")).findFirst().orElseThrow().linhas().stream().filter(i->i.natureza().equals("OPERACAO")).findFirst().orElseThrow().total()));
        String url="/aratech/pcp/lotes/producao/"+id+"/liberar";
        var corteSomente=new Liberar(Set.of(setor("CORTE")),1);
        var parcial=enviar(post(url),pcp,corteSomente);assertEquals(1,parcial.get("ordens").size());assertEquals("LIBERACAO_PARCIAL",parcial.get("status").asText());
        mvc.perform(post("/aratech/pcp/lotes/producao/"+id+"/revisar").session(pcp).header("X-XSRF-TOKEN",csrf(pcp)).contentType("application/json").content("{\"versao\":1}")).andExpect(status().isConflict());
        assertEquals(1,enviar(post(url),pcp,corteSomente).get("ordens").size());
        var novaFicha=FabricacaoTest.ficha(variante.dados().ficha().codigo(),"Modelo alterado",0,FabricacaoTest.painel(variante.dados().ficha().itens().getFirst().chave(),"LAT",new BigDecimal("1000")));
        var alterada=new FichaVariante(variante.dados().modeloId(),"BRANCO","Branco alterado",true,1,novaFicha,List.of());
        enviar(put("/aratech/administracao/guarda-roupas/variantes/"+variante.id()),admin,alterada);
        var liberado=enviar(post(url),pcp,new Liberar(Set.of(setor("ALMOXARIFADO")),1));assertEquals(2,liberado.get("ordens").size());assertEquals("Branco",liberado.get("variante").asText());
        var almox=sessao("ALMOXARIFADO_REQUISICOES.CONSULTAR","ALMOXARIFADO_REQUISICOES.ALTERAR","ALMOXARIFADO_REQUISICOES.FINALIZAR");
        var ordens=json.treeToValue(liberado,Lote.class).ordens();var oc=ordens.stream().filter(o->o.setorId()==setor("CORTE")).findFirst().orElseThrow();var oa=ordens.stream().filter(o->o.setorId()==setor("ALMOXARIFADO")).findFirst().orElseThrow();
        var dadosCorte=mvc.perform(get("/aratech/corte/ordens/"+oc.id()).session(corte)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(dadosCorte.contains("Dobradiça"));assertFalse(dadosCorte.contains("Furos exclusivos"));
        assertTrue(dadosCorte.contains("2200"));assertTrue(dadosCorte.contains("Modelo X"));
        var dadosAlmox=mvc.perform(get("/aratech/almoxarifado/separacoes/"+oa.id()).session(almox)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(dadosAlmox.contains("Corte branco"));assertFalse(dadosAlmox.contains("2200"));assertEquals(1200,json.readTree(dadosAlmox).at("/linhas/0/total").asInt());
        mvc.perform(get("/aratech/corte/ordens/"+oa.id()).session(corte)).andExpect(status().isNotFound());
        mvc.perform(get("/aratech/almoxarifado/separacoes").session(corte)).andExpect(status().isForbidden());
        mvc.perform(post(url).session(corte).header("X-XSRF-TOKEN",csrf(corte)).contentType("application/json").content(json.writeValueAsString(corteSomente))).andExpect(status().isForbidden());
        assertEquals(2000,jdbc.queryForObject("SELECT quantidade FROM produto WHERE id=?",Integer.class,produto));
        enviar(post("/aratech/almoxarifado/separacoes/"+oa.id()+"/iniciar"),almox,new Acao(1));
        var cmd=new Consumo(oa.linhas().getFirst().itemChave(),UUID.randomUUID(),"Conferido pela equipe");
        jdbc.update("UPDATE produto SET quantidade=1199 WHERE id=?",produto);
        mvc.perform(post("/aratech/almoxarifado/separacoes/"+oa.id()+"/confirmar").session(almox).header("X-XSRF-TOKEN",csrf(almox)).contentType("application/json").content(json.writeValueAsString(cmd))).andExpect(status().isConflict());
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM pcp_consumo_setor WHERE ordem_id=?",Integer.class,oa.id()));
        assertEquals(1199,jdbc.queryForObject("SELECT quantidade FROM produto WHERE id=?",Integer.class,produto));
        jdbc.update("UPDATE produto SET quantidade=2000 WHERE id=?",produto);
        em.clear();
        enviar(post("/aratech/almoxarifado/separacoes/"+oa.id()+"/confirmar"),almox,cmd);
        enviar(post("/aratech/almoxarifado/separacoes/"+oa.id()+"/confirmar"),almox,cmd);
        assertEquals(800,jdbc.queryForObject("SELECT quantidade FROM produto WHERE id=?",Integer.class,produto));
        enviar(post("/aratech/almoxarifado/separacoes/"+oa.id()+"/concluir"),almox,new Acao(3));
        var atual=json.readTree(mvc.perform(get("/aratech/pcp/lotes/producao/"+id).session(pcp)).andReturn().getResponse().getContentAsString());assertEquals("LIBERACAO_PARCIAL",atual.get("status").asText());
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM pcp_ordem_setor WHERE lote_id=? AND setor_id=?",Integer.class,id,setor("CORTE")));
        var total=enviar(post(url),pcp,new Liberar(Set.of(setor("CORTE"),setor("ALMOXARIFADO"),setor("FURACAO")),1));assertEquals(3,total.get("ordens").size());
        var furacao=sessao("PRODUCAO_FURACAO.CONSULTAR","PRODUCAO_FURACAO.ALTERAR","PRODUCAO_FURACAO.FINALIZAR");
        for(var o:json.treeToValue(total,Lote.class).ordens())if(!o.status().equals("CONCLUIDA")){String base=o.setorId()==setor("CORTE")?"/aratech/corte/ordens/":"/aratech/producao/furacao/";var sessao=o.setorId()==setor("CORTE")?corte:furacao;enviar(post(base+o.id()+"/iniciar"),sessao,new Acao(1));enviar(post(base+o.id()+"/concluir"),sessao,new Acao(2));}
        mvc.perform(get("/aratech/pcp/lotes/producao/"+id).session(pcp)).andExpect(status().isOk()).andExpect(jsonPath("status").value("CONCLUIDO"));
    }
    @Test void bloqueiaSemRoteiroEValidaSetoresERevisao() throws Exception {
        var v=prepararVariante(produto());enviar(put("/aratech/administracao/guarda-roupas/variantes/"+v.id()),admin,new FichaVariante(v.dados().modeloId(),"BRANCO","Branco",true,1,v.dados().ficha(),List.of()));
        var l=planejar(v);assertEquals("BLOQUEADO",l.get("status").asText());assertTrue(l.at("/previa/pendencias").size()>0);
        mvc.perform(post("/aratech/pcp/lotes/producao/"+l.get("id").asLong()+"/liberar").session(pcp).header("X-XSRF-TOKEN",csrf(pcp)).contentType("application/json").content(json.writeValueAsString(new Liberar(Set.of(setor("CORTE")),2)))).andExpect(status().isConflict());
        enviar(put("/aratech/administracao/guarda-roupas/variantes/"+v.id()),admin,new FichaVariante(v.dados().modeloId(),"BRANCO","Branco",true,2,v.dados().ficha(),v.dados().roteiro()));
        var corrigido=enviar(post("/aratech/pcp/lotes/producao/"+l.get("id").asLong()+"/revisar"),pcp,new Acao(2));assertEquals(3,corrigido.get("revisao").asInt());assertEquals(0,corrigido.at("/previa/pendencias").size());assertEquals(l.get("id").asLong(),corrigido.get("id").asLong());
        enviar(post("/aratech/pcp/lotes/producao/"+l.get("id").asLong()+"/liberar"),pcp,new Liberar(Set.of(setor("CORTE")),3));
    }
    @Test void limitaQuantidadeDaOrigemERepeteCriacaoSemDuplicar() throws Exception {
        var v=prepararVariante(produto());long origem=jdbc.queryForObject("INSERT INTO pcp_ordem(produto,quantidade,inicio,previsao,responsavel,horas_previstas,status) VALUES ('Modelo X',150,CURRENT_DATE,CURRENT_DATE+2,'PCP',1,'EM_PRODUCAO') RETURNING id",Long.class);
        var d=new NovoLote(v.id(),origem,150,LocalDate.now().plusDays(2),"Origem",UUID.randomUUID());var l=enviar(post("/aratech/pcp/lotes/producao"),pcp,d);assertEquals(l.get("id").asLong(),enviar(post("/aratech/pcp/lotes/producao"),pcp,d).get("id").asLong());
        mvc.perform(post("/aratech/pcp/lotes/producao").session(pcp).header("X-XSRF-TOKEN",csrf(pcp)).contentType("application/json").content(json.writeValueAsString(new NovoLote(v.id(),origem,1,d.previsao(),"Excesso",UUID.randomUUID())))).andExpect(status().isConflict());
        var ordens=sessao("PCP_ORDENS.FINALIZAR");var concluir=new com.aratech.moveis.shared.dto.Comando(UUID.randomUUID(),0L,"Concluir ordem de teste",null,null,null);
        mvc.perform(post("/aratech/pcp/ordens/"+origem+"/concluir").session(ordens).header("X-XSRF-TOKEN",csrf(ordens)).contentType("application/json").content(json.writeValueAsString(concluir))).andExpect(status().isConflict());
        mvc.perform(post("/aratech/pcp/lotes/producao/"+l.get("id").asLong()+"/liberar").session(pcp).header("X-XSRF-TOKEN",csrf(pcp)).contentType("application/json").content(json.writeValueAsString(new Liberar(Set.of(999999L),1)))).andExpect(status().isBadRequest());
    }
}
