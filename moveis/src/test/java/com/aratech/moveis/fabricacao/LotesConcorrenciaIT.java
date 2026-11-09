package com.aratech.moveis.fabricacao;

import com.aratech.moveis.fabricacao.repository.FabricacaoRepository;
import com.aratech.moveis.pcp.lotes.*;
import com.aratech.moveis.pcp.lotes.repository.LotesProducaoRepository;
import static com.aratech.moveis.pcp.lotes.ProducaoDtos.*;
import com.aratech.moveis.shared.operacao.service.OperacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest @ActiveProfiles("dev")
class LotesConcorrenciaIT {
 @Autowired LotesProducaoRepository repo; @Autowired FabricacaoRepository modelos; @Autowired JdbcTemplate jdbc; @Autowired PlatformTransactionManager manager;
 @Test void duasTransacoesConcorrentesLiberamUmaUnicaOrdemPorSetor() throws Exception {
  var tx=new TransactionTemplate(manager);var service=new com.aratech.moveis.pcp.lotes.imp.LotesProducaoServiceImp(repo,modelos,mock(OperacaoService.class),null);long[] ids=new long[3];var pool=Executors.newFixedThreadPool(2);
  try {
   tx.executeWithoutResult(status->{
    var chave=UUID.randomUUID();var ficha=FabricacaoTest.ficha("CONCORRENCIA-"+UUID.randomUUID(),"Concorrência isolada",0,FabricacaoTest.painel(chave,"LAT",new BigDecimal("2200")));
    ids[0]=modelos.criar(ficha);long corte=service.setores().stream().filter(s->s.codigo().equals("CORTE")).findFirst().orElseThrow().id();
    var dados=new FichaVariante(ids[0],"B","Branco",true,0,ficha,List.of(new Roteiro(chave,corte,null,List.of(new Operacao(corte,"Cortar","Conferir medidas")))));
    ids[1]=repo.salvar(null,dados);var v=repo.variante(ids[1],false);ids[2]=repo.criar(new NovoLote(ids[1],null,150,LocalDate.now().plusDays(2),"Teste concorrente",UUID.randomUUID()),v,service.calcular(v,150),"Teste de concorrência");
   });
   long setor=service.setores().stream().filter(s->s.codigo().equals("CORTE")).findFirst().orElseThrow().id();var pronto=new CountDownLatch(2);var iniciar=new CountDownLatch(1);
   Callable<Lote> operacao=()->{pronto.countDown();assertTrue(iniciar.await(30,TimeUnit.SECONDS));return tx.execute(status->service.liberar(ids[2],new Liberar(Set.of(setor),1)));};
   var a=pool.submit(operacao);var b=pool.submit(operacao);assertTrue(pronto.await(30,TimeUnit.SECONDS));iniciar.countDown();var primeiro=a.get(45,TimeUnit.SECONDS);var segundo=b.get(45,TimeUnit.SECONDS);
   assertEquals(1,primeiro.ordens().size());assertEquals(primeiro.ordens().getFirst().id(),segundo.ordens().getFirst().id());assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM pcp_ordem_setor WHERE lote_id=?",Integer.class,ids[2]));assertEquals(1,segundo.ordens().getFirst().linhas().size());assertEquals(0,new BigDecimal("300").compareTo(segundo.ordens().getFirst().linhas().getFirst().total()));
  } finally {
   pool.shutdownNow();pool.awaitTermination(10,TimeUnit.SECONDS);
   tx.executeWithoutResult(status->{if(ids[2]>0){jdbc.update("DELETE FROM pcp_ordem_setor WHERE lote_id=?",ids[2]);jdbc.update("DELETE FROM pcp_lote_producao WHERE id=?",ids[2]);}if(ids[1]>0)jdbc.update("DELETE FROM fabricacao_variante WHERE id=?",ids[1]);if(ids[0]>0)jdbc.update("DELETE FROM fabricacao_modelo WHERE id=?",ids[0]);});
  }
 }
}
