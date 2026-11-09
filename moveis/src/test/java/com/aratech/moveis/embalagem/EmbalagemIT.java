package com.aratech.moveis.embalagem;

import com.aratech.moveis.embalagem.dto.*;
import com.aratech.moveis.fabricacao.dto.*;
import com.aratech.moveis.security.authorization.*;
import com.aratech.moveis.security.service.ContasService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("dev") @Transactional
class EmbalagemIT {
    @Autowired MockMvc mvc;@Autowired JdbcTemplate jdbc;@Autowired JsonMapper json;@Autowired ContasService contas;
    MockHttpSession recebedor,operador;
    @BeforeEach void preparar(){recebedor=sessao("EMBALAGEM_LOTES.CONSULTAR","EMBALAGEM_LOTES.CADASTRAR","EMBALAGEM_LOTES.ALTERAR");operador=sessao("EMBALAGEM_ESTEIRAS.CONSULTAR","EMBALAGEM_ESTEIRAS.CADASTRAR","EMBALAGEM_ESTEIRAS.ALTERAR","EMBALAGEM_ESTEIRAS.FINALIZAR");}
    MockHttpSession sessao(String...permissoes){long id=jdbc.queryForObject("INSERT INTO funcionario(nome_completo,email,ativo) VALUES ('Operador embalagem',?,TRUE) RETURNING id",Long.class,UUID.randomUUID()+"@aratechmoveis");jdbc.update("INSERT INTO acesso_conta(funcionario_id,senha_hash) VALUES (?,?)",id,"nao-usada");long perfil=jdbc.queryForObject("INSERT INTO perfil_acesso(nome) VALUES (?) RETURNING id",Long.class,"EMB-"+UUID.randomUUID());jdbc.update("INSERT INTO funcionario_perfil_acesso(funcionario_id,perfil_id) VALUES (?,?)",id,perfil);for(String p:permissoes)jdbc.update("INSERT INTO perfil_permissao_acesso(perfil_id,permissao) VALUES (?,?)",perfil,p);var u=contas.carregar(id,Instant.now());var c=SecurityContextHolder.createEmptyContext();c.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(u,null,SessaoAuthorities.authorities(u)));var s=new MockHttpSession();s.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,c);return s;}
    String csrf(MockHttpSession s)throws Exception{return json.readTree(mvc.perform(get("/aratech/auth/csrf").session(s)).andReturn().getResponse().getContentAsString()).path("token").asText();}
    JsonNode enviar(MockHttpServletRequestBuilder req,MockHttpSession s,Object dto,int status)throws Exception{return json.readTree(mvc.perform(req.session(s).header("X-XSRF-TOKEN",csrf(s)).contentType("application/json").content(json.writeValueAsString(dto))).andExpect(status().is(status)).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));}
    long esteira(String nome)throws Exception{return enviar(post("/aratech/embalagem/esteiras"),operador,new EsteiraEntrada("E-"+UUID.randomUUID(),nome,nome,"Equipe teste",1,true,"",0),200).path("id").asLong();}
    long origem(String status){var item=new ItemGuardaRoupa(UUID.randomUUID(),"LAT","Lateral",ItemGuardaRoupa.Tipo.PAINEL,"Estrutura","MDF","Branco",ItemGuardaRoupa.Unidade.UN,BigDecimal.valueOf(2),true,BigDecimal.valueOf(2200),BigDecimal.valueOf(600),BigDecimal.valueOf(18),ItemGuardaRoupa.Veio.SEM,Set.of(),"","","",Map.of());var ficha=new FichaGuardaRoupa("G-"+UUID.randomUUID(),"Aurora embalagem","",BigDecimal.valueOf(1800),BigDecimal.valueOf(2200),BigDecimal.valueOf(600),true,0,List.of(item));long modelo=jdbc.queryForObject("INSERT INTO fabricacao_modelo(codigo,nome,ficha) VALUES (?,?,?::jsonb) RETURNING id",Long.class,ficha.codigo(),ficha.nome(),json.writeValueAsString(ficha));return jdbc.queryForObject("INSERT INTO fabricacao_lote(modelo_id,modelo_versao,quantidade,previsao,ficha,status,chave_operacao,autor_id,autor_nome) VALUES (?,1,2,CURRENT_DATE,?::jsonb,?,?,1,'Teste') RETURNING id",Long.class,modelo,json.writeValueAsString(new ModeloGuardaRoupa(modelo,1,ficha,List.of())),status,UUID.randomUUID());}
    NovoLoteEmbalagem entrada(long origem,long a,long b){return new NovoLoteEmbalagem(origem,LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(1),NovoLoteEmbalagem.Prioridade.NORMAL,"Conferir volumes",true,UUID.randomUUID(),List.of(new EtapaEntrada(a,"Proteção","Aplicar proteção"),new EtapaEntrada(b,"Fechamento e identificação","Conferir os volumes")));}
    ComandoEmbalagem comando(long versao){return new ComandoEmbalagem(versao,"",null,false,false,false);}
    String acao(long esteira,long etapa,String acao){return "/aratech/embalagem/esteiras/"+esteira+"/etapas/"+etapa+"/"+acao;}
    @Test void distribuiTrabalhoRespeitaSequenciaCapacidadePausasEConferenciaFinal()throws Exception{
        long a=esteira("Proteção"),b=esteira("Fechamento");var l1=enviar(post("/aratech/embalagem/lotes"),recebedor,entrada(origem("CORTADO"),a,b),200);var l2=enviar(post("/aratech/embalagem/lotes"),recebedor,entrada(origem("CORTADO"),a,b),200);
        long id=l1.path("id").asLong(),e1=l1.path("etapas").get(0).path("id").asLong(),e2=l1.path("etapas").get(1).path("id").asLong(),outro=l2.path("etapas").get(0).path("id").asLong();
        enviar(post(acao(a,e1,"iniciar")),operador,comando(1),200);
        enviar(post(acao(b,e2,"iniciar")),operador,comando(2),409);
        enviar(post(acao(a,outro,"iniciar")),operador,comando(1),409);
        var pausado=enviar(post(acao(a,e1,"pausar")),operador,new ComandoEmbalagem(2,"Aguardando material",null,false,false,false),200);assertEquals("PAUSADO",pausado.path("status").asText());
        enviar(post(acao(a,outro,"iniciar")),operador,comando(1),409);
        enviar(put("/aratech/embalagem/esteiras/"+a),operador,new EsteiraEntrada("E-PAUSA","Proteção","Proteção","Equipe",1,false,"",1),409);
        enviar(post(acao(a,e1,"retomar")),operador,comando(3),200);
        enviar(post(acao(a,e1,"concluir")),operador,comando(4),200);
        enviar(post(acao(a,outro,"iniciar")),operador,comando(1),200);
        enviar(post(acao(b,e2,"iniciar")),operador,comando(5),200);
        mvc.perform(get("/aratech/embalagem/esteiras").session(operador)).andExpect(status().isOk());
        assertEquals(2L,jdbc.queryForObject("SELECT count(*) FROM embalagem_etapa WHERE id IN (?,?) AND status='EM_EXECUCAO'",Long.class,outro,e2));
        enviar(post(acao(b,e2,"concluir")),operador,comando(6),400);
        var concluido=enviar(post(acao(b,e2,"concluir")),operador,new ComandoEmbalagem(6,"",4,true,true,true),200);
        assertEquals("EMBALADO",concluido.path("status").asText());assertEquals(4,concluido.path("volumes").asInt());assertFalse(concluido.path("concluidoEm").isNull());
        assertEquals(7,concluido.path("historico").size());assertTrue(concluido.path("etapas").get(0).path("segundosAtivos").asLong()>=0);
        enviar(post(acao(b,e2,"concluir")),operador,new ComandoEmbalagem(6,"",4,true,true,true),409);
        mvc.perform(get("/aratech/embalagem/lotes/"+id).session(recebedor)).andExpect(status().isOk()).andExpect(jsonPath("modelo.ficha.nome").value("Aurora embalagem"));
    }
    @Test void recebimentoEhIdempotenteEOrigemPodeSerRecebidaNovamenteAposCancelamento()throws Exception{
        long a=esteira("Única"),o=origem("CORTADO");var dto=entrada(o,a,a);var lote=enviar(post("/aratech/embalagem/lotes"),recebedor,dto,200);long id=lote.path("id").asLong();
        assertEquals(id,enviar(post("/aratech/embalagem/lotes"),recebedor,dto,200).path("id").asLong());
        enviar(post("/aratech/embalagem/lotes"),recebedor,entrada(o,a,a),409);
        enviar(post("/aratech/embalagem/lotes/"+id+"/cancelar"),recebedor,new ComandoEmbalagem(1,"Roteiro será replanejado",null,false,false,false),200);
        assertNotEquals(id,enviar(post("/aratech/embalagem/lotes"),recebedor,entrada(o,a,a),200).path("id").asLong());
        assertEquals(2L,jdbc.queryForObject("SELECT count(*) FROM embalagem_lote WHERE origem_id=?",Long.class,o));
        enviar(post("/aratech/embalagem/lotes"),recebedor,entrada(origem("EM_CORTE"),a,a),409);
        var semConfirmacao=new NovoLoteEmbalagem(origem("CORTADO"),dto.previsao(),dto.prioridade(),"",false,UUID.randomUUID(),dto.etapas());
        enviar(post("/aratech/embalagem/lotes"),recebedor,semConfirmacao,400);
    }
    @Test void permissoesDeLotesEEsteirasSaoIndependentes()throws Exception{
        mvc.perform(get("/aratech/embalagem/lotes/esteiras").session(recebedor)).andExpect(status().isOk());
        mvc.perform(get("/aratech/embalagem/esteiras").session(recebedor)).andExpect(status().isForbidden());
        mvc.perform(get("/aratech/embalagem/lotes").session(operador)).andExpect(status().isForbidden());
        var corte=sessao("CORTE_LOTES.CONSULTAR");mvc.perform(get("/aratech/embalagem/lotes").session(corte)).andExpect(status().isForbidden());
        assertEquals("EMBALAGEM_ESTEIRAS.ALTERAR",Permissoes.necessaria(acao(1,1,"pausar"),"POST"));
        assertEquals("EMBALAGEM_ESTEIRAS.ALTERAR",Permissoes.necessaria(acao(1,1,"retomar"),"POST"));
        assertEquals("EMBALAGEM_ESTEIRAS.FINALIZAR",Permissoes.necessaria(acao(1,1,"concluir"),"POST"));
    }
}
