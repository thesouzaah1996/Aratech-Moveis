package com.aratech.moveis.fabricacao;

import com.aratech.moveis.fabricacao.dto.*;
import com.aratech.moveis.pcp.lotes.*;
import com.aratech.moveis.pcp.lotes.repository.LotesProducaoRepository;
import static com.aratech.moveis.pcp.lotes.ProducaoDtos.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LotesProducaoTest {
 Setor corte=new Setor(1,"CORTE","Corte","CORTE_LOTES","/aratech/corte/ordens","OPERACAO",true);
 Setor almox=new Setor(2,"ALMOXARIFADO","Almoxarifado","ALMOXARIFADO_REQUISICOES","/aratech/almoxarifado/separacoes","SEPARACAO",false);
 Setor furacao=new Setor(3,"FURACAO","Furação","PRODUCAO_FURACAO","/aratech/producao/furacao","OPERACAO",false);
 LotesProducaoService service(){var repo=mock(LotesProducaoRepository.class);when(repo.setores()).thenReturn(List.of(corte,almox,furacao));return new com.aratech.moveis.pcp.lotes.imp.LotesProducaoServiceImp(repo,null,null,null);}
 @Test void consumoEmSetorComMedidasPreservaEspecificacaoEBloqueiaDadosIncompletos(){
  UUID chave=UUID.randomUUID();var painel=FabricacaoTest.painel(chave,"LAT",new BigDecimal("2200"));
  var roteiro=List.of(new Roteiro(chave,1L,null,List.of()));
  var calculo=service().calcular(new Variante(1,1,new FichaVariante(1,"B","Branco",true,0,FabricacaoTest.ficha("X","Modelo",0,painel),roteiro)),150);
  var linha=calculo.setores().getFirst().linhas().getFirst();
  assertNotNull(linha.especificacao());assertEquals(painel.comprimentoMm(),linha.especificacao().comprimentoMm());
  assertEquals("",linha.especificacao().furacao());assertEquals(0,new BigDecimal("300").compareTo(linha.total()));
  assertTrue(calculo.pendencias().stream().anyMatch(e->e.contains("sem operação")));
  var incompleto=new ItemGuardaRoupa(chave,"ITEM","Item",ItemGuardaRoupa.Tipo.OUTRO,"Peças","MDF","",ItemGuardaRoupa.Unidade.UN,BigDecimal.ONE,false,null,null,null,ItemGuardaRoupa.Veio.SEM,Set.of(),"","Instrução de outro setor","",Map.of());
  var bloqueado=service().calcular(new Variante(1,1,new FichaVariante(1,"B","Branco",true,0,FabricacaoTest.ficha("X","Modelo",0,incompleto),roteiro)),150);
  assertTrue(bloqueado.pendencias().stream().anyMatch(e->e.contains("faltam medidas")));
 }
 @Test void multiplicaDecimaisSemArredondarEConverteExplicitamenteParaEstoqueInteiro(){
  UUID chave=UUID.randomUUID();var item=new ItemGuardaRoupa(chave,"COLA","Cola",ItemGuardaRoupa.Tipo.ACABAMENTO,"Insumos","Cola","Incolor",ItemGuardaRoupa.Unidade.KG,new BigDecimal("0.125"),false,null,null,null,ItemGuardaRoupa.Veio.SEM,Set.of(),"","","",Map.of());
  var ficha=FabricacaoTest.ficha("X","Modelo",0,item);var v=new Variante(1,1,new FichaVariante(1,"B","Branco",true,0,ficha,List.of(new Roteiro(chave,2L,5L,List.of(),new BigDecimal("1000")))));
  var c=service().calcular(v,150);assertTrue(c.pendencias().isEmpty());var linha=c.setores().getFirst().linhas().getFirst();assertEquals(0,new BigDecimal("18.750").compareTo(linha.total()));assertEquals(0,new BigDecimal("18750").compareTo(linha.quantidadeEstoque()));assertEquals("KG",linha.unidade());
 }
 @Test void operacoesEmDoisSetoresNaoDuplicamConsumoNemRevelamInstrucoesAlheias(){
  UUID chave=UUID.randomUUID();var item=FabricacaoTest.painel(chave,"LAT",new BigDecimal("2200"));var ficha=FabricacaoTest.ficha("X","Modelo",0,item);var v=new Variante(1,1,new FichaVariante(1,"B","Branco",true,0,ficha,List.of(new Roteiro(chave,1L,null,List.of(new Operacao(1,"Cortar","Corte branco"),new Operacao(3,"Furar","Furos em 32 mm"))))));
  var c=service().calcular(v,150);assertTrue(c.pendencias().isEmpty());assertEquals(2,c.setores().size());assertEquals(1,c.setores().getFirst().linhas().size());assertEquals(1,c.setores().stream().flatMap(g->g.linhas().stream()).filter(Linha::consomeMaterial).count());var corte=c.setores().getFirst().linhas().getFirst();assertEquals("Corte branco",corte.instrucoes());assertEquals("",corte.especificacao().furacao());assertEquals("",corte.especificacao().observacoes());assertEquals(0,new BigDecimal("300").compareTo(corte.total()));
 }
 @Test void identificaSetorObrigatorioAusenteEConversaoNaoInteira(){
  UUID chave=UUID.randomUUID();var item=new ItemGuardaRoupa(chave,"COLA","Cola",ItemGuardaRoupa.Tipo.ACABAMENTO,"Insumos","Cola","Incolor",ItemGuardaRoupa.Unidade.KG,new BigDecimal("0.125"),false,null,null,null,ItemGuardaRoupa.Veio.SEM,Set.of(),"","","",Map.of());var ficha=FabricacaoTest.ficha("X","Modelo",0,item);
  assertFalse(service().calcular(new Variante(1,1,new FichaVariante(1,"B","Branco",true,0,ficha,List.of())),150).pendencias().isEmpty());
  var c=service().calcular(new Variante(1,1,new FichaVariante(1,"B","Branco",true,0,ficha,List.of(new Roteiro(chave,2L,5L,List.of(),BigDecimal.ONE)))),1);assertTrue(c.pendencias().stream().anyMatch(e->e.contains("conversão exata")));
 }
}
