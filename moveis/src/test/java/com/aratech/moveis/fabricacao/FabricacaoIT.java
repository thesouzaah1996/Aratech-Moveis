package com.aratech.moveis.fabricacao;

import com.aratech.moveis.fabricacao.dto.*;
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
class FabricacaoIT {
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
    FichaGuardaRoupa ficha(UUID item,String codigo,String nome,long versao,int comprimento){return FabricacaoTest.ficha(codigo,nome,versao,FabricacaoTest.painel(item,"LAT",new BigDecimal(comprimento)));}
    @Test void fichaFotosELotePreservamRevisaoEFluxoEntreSetores() throws Exception {
        UUID item=UUID.randomUUID();String codigo="G-"+UUID.randomUUID();
        long modelo=enviar(post("/aratech/administracao/guarda-roupas"),admin,ficha(item,codigo,"Aurora original",0,2200)).get("id").asLong();
        var imagem=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(3,2,BufferedImage.TYPE_INT_RGB),"png",imagem);
        var upload=multipart("/aratech/administracao/guarda-roupas/"+modelo+"/fotos").file(new MockMultipartFile("arquivo","lateral.png","image/png",imagem.toByteArray())).param("versao","1").param("itemChave",item.toString()).param("legenda","Lateral original");
        var m=json.readTree(mvc.perform(upload.session(admin).header("X-XSRF-TOKEN",csrf(admin))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(2,m.get("versao").asLong());long foto=m.get("fotos").get(0).get("id").asLong();
        var novo=new NovoLoteCorte(modelo,3,LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(1),"Teste integração",UUID.randomUUID());
        var lote=enviar(post("/aratech/pcp/lotes/corte"),pcp,novo);long id=lote.get("id").asLong();
        assertEquals(id,enviar(post("/aratech/pcp/lotes/corte"),pcp,novo).get("id").asLong());
        mvc.perform(get("/aratech/corte/lotes/"+id).session(corte)).andExpect(status().isNotFound());
        enviar(put("/aratech/administracao/guarda-roupas/"+modelo),admin,ficha(item,codigo,"Modelo alterado",2,1000));
        mvc.perform(delete("/aratech/administracao/guarda-roupas/"+modelo+"/fotos/"+foto).param("versao","3").session(admin).header("X-XSRF-TOKEN",csrf(admin))).andExpect(status().isOk()).andExpect(jsonPath("fotos").isEmpty());
        mvc.perform(get("/aratech/pcp/lotes/corte/"+id).session(pcp)).andExpect(status().isOk())
                .andExpect(jsonPath("modelo.ficha.nome").value("Aurora original"))
                .andExpect(jsonPath("modelo.ficha.itens[0].comprimentoMm").value(2200))
                .andExpect(jsonPath("modeloVersao").value(2))
                .andExpect(jsonPath("modelo.fotos[0].id").value(foto));
        enviar(post("/aratech/pcp/lotes/corte/"+id+"/enviar"),pcp,new AcaoLoteCorte(1));
        mvc.perform(get("/aratech/corte/lotes/"+id).session(corte)).andExpect(status().isOk()).andExpect(jsonPath("quantidade").value(3));
        mvc.perform(get("/aratech/corte/lotes/modelos/"+modelo+"/fotos/"+foto).session(corte)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        mvc.perform(post("/aratech/corte/lotes/"+id+"/finalizar").session(corte).header("X-XSRF-TOKEN",csrf(corte)).contentType("application/json").content("{\"versao\":2}")).andExpect(status().isConflict());
        enviar(post("/aratech/corte/lotes/"+id+"/iniciar"),corte,new AcaoLoteCorte(2));
        var concluido=enviar(post("/aratech/corte/lotes/"+id+"/finalizar"),corte,new AcaoLoteCorte(3));
        assertEquals("CORTADO",concluido.get("status").asText());assertFalse(concluido.get("concluidoEm").isNull());
        assertEquals(1L,jdbc.queryForObject("SELECT count(*) FROM fabricacao_lote WHERE modelo_id=?",Long.class,modelo));
        assertTrue(jdbc.queryForObject("SELECT count(*) FROM gestao_auditoria WHERE entidade='LOTE_CORTE' AND registro_id=?",Long.class,id)>=4);
    }
    @Test void setoresNaoPodemAlterarCatalogoNemReceberLotePorPermissaoComercial() throws Exception {
        mvc.perform(get("/aratech/administracao/guarda-roupas").session(corte)).andExpect(status().isForbidden());
        mvc.perform(post("/aratech/pcp/lotes/corte").session(corte).header("X-XSRF-TOKEN",csrf(corte)).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        var comercial=sessao("COMERCIAL_PEDIDOS.CONSULTAR");
        mvc.perform(get("/aratech/corte/lotes").session(comercial)).andExpect(status().isForbidden());
        mvc.perform(get("/aratech/pcp/lotes/modelos").session(pcp)).andExpect(status().isOk());
        mvc.perform(get("/aratech/pcp/lotes").session(pcp)).andExpect(status().isOk());
    }
    @Test void recusaMedidasAusentesImagemFalsaEEdicaoComVersaoAntiga() throws Exception {
        UUID item=UUID.randomUUID();String codigo="G-"+UUID.randomUUID();
        long modelo=enviar(post("/aratech/administracao/guarda-roupas"),admin,ficha(item,codigo,"Modelo validado",0,2200)).get("id").asLong();
        mvc.perform(put("/aratech/administracao/guarda-roupas/"+modelo).session(admin).header("X-XSRF-TOKEN",csrf(admin)).contentType("application/json").content(json.writeValueAsString(ficha(item,codigo,"Versão antiga",0,2200)))).andExpect(status().isConflict());
        var falso=multipart("/aratech/administracao/guarda-roupas/"+modelo+"/fotos").file(new MockMultipartFile("arquivo","foto.png","image/png",new byte[]{1,2,3})).param("versao","1");
        mvc.perform(falso.session(admin).header("X-XSRF-TOKEN",csrf(admin))).andExpect(status().isBadRequest());
        mvc.perform(post("/aratech/administracao/guarda-roupas").session(admin).header("X-XSRF-TOKEN",csrf(admin)).contentType("application/json").content(json.writeValueAsString(FabricacaoTest.ficha("OUTRO-"+codigo,"Incompleto",0,FabricacaoTest.painel(item,"LAT",null))))).andExpect(status().isBadRequest());
        assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM fabricacao_foto WHERE modelo_id=?",Long.class,modelo));
    }
}
