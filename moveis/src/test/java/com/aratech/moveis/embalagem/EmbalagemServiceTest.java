package com.aratech.moveis.embalagem;

import com.aratech.moveis.embalagem.dto.*;
import com.aratech.moveis.embalagem.repository.EmbalagemRepository;
import com.aratech.moveis.embalagem.service.EmbalagemService;
import com.aratech.moveis.fabricacao.repository.FabricacaoRepository;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbalagemServiceTest {
 final EmbalagemRepository repo=mock(EmbalagemRepository.class);
 final FabricacaoRepository fabricacao=mock(FabricacaoRepository.class);
 final EmbalagemService service=new com.aratech.moveis.embalagem.service.imp.EmbalagemServiceImp(repo,fabricacao,base());
 void lote(String status,boolean liberada){
  when(repo.esteira(2,true)).thenReturn(new Esteira(2,"E2","Esteira","Proteger","Equipe",1,true,"",1));
  when(repo.loteDaEtapa(3)).thenReturn(1L);
  var etapa=new EtapaEmbalagem(3,1,1,2,"Esteira","Proteger","",status,"",0,null,null,"",liberada);
  when(repo.lote(1,true)).thenReturn(new LoteEmbalagem(1,8,"EM_EMBALAGEM",1,150,LocalDate.now().plusDays(1),"NORMAL","",null,Instant.now(),null,null,List.of(etapa),List.of()));
 }
 ComandoEmbalagem cmd(){return new ComandoEmbalagem(1,null,null,false,false,false);}
 @Test void etapaFuturaNaoInicia(){lote("AGUARDANDO",false);assertThrows(AuthException.class,()->service.operar(2,3,"iniciar",cmd()));verify(repo,never()).operar(anyLong(),any(),any(),any());}
 @Test void etapaPausadaTambemOcupaCapacidade(){lote("AGUARDANDO",true);when(repo.ocupadas(2)).thenReturn(1);assertThrows(AuthException.class,()->service.operar(2,3,"iniciar",cmd()));verify(repo,never()).operar(anyLong(),any(),any(),any());}
 @Test void ultimaEtapaExigeVolumesETodasAsConferencias(){
  lote("EM_EXECUCAO",true);
  assertEquals(400,assertThrows(AuthException.class,()->service.operar(2,3,"concluir",new ComandoEmbalagem(1,null,10,true,true,false))).status());
  verify(repo,never()).statusLote(anyLong(),any(),any());
 }
 @Test void ultimaEtapaConcluidaRegistraVolumesEHistorico(){
  lote("EM_EXECUCAO",true);service.operar(2,3,"concluir",new ComandoEmbalagem(1,null,300,true,true,true));
  verify(repo).operar(eq(3L),eq("concluir"),any(),any());verify(repo).statusLote(1,"EMBALADO",300);
  verify(repo).evento(eq(1L),eq(3L),eq("CONCLUIR"),contains("300"),any());
 }
 @Test void naoReduzCapacidadeAbaixoDeExecucoesEPausas(){
  when(repo.esteira(2,true)).thenReturn(new Esteira(2,"E2","Esteira","Proteger","Equipe",3,true,"",1));when(repo.ocupadas(2)).thenReturn(2);
  assertThrows(AuthException.class,()->service.salvarEsteira(2L,new EsteiraEntrada("E2","Esteira","Proteger","Equipe",1,true,"",1)));
  verify(repo,never()).editarEsteira(anyLong(),any());
 }
 @Test void recebimentoExigeConfirmacaoDeProntidao(){
  assertThrows(AuthException.class,()->service.receber(new NovoLoteEmbalagem(8,LocalDate.now().plusDays(1),NovoLoteEmbalagem.Prioridade.NORMAL,"",false,UUID.randomUUID(),List.of(new EtapaEntrada(2,"Proteger","")))));
  verifyNoInteractions(fabricacao);verify(repo,never()).criarLote(any(),any());
 }
}
