package com.aratech.moveis.fabricacao;

import com.aratech.moveis.fabricacao.dto.*;
import com.aratech.moveis.fabricacao.repository.FabricacaoRepository;
import com.aratech.moveis.fabricacao.service.imp.FabricacaoServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FabricacaoServiceImpTest {
 final FabricacaoRepository repo=mock(FabricacaoRepository.class);
 final FabricacaoServiceImp service=new FabricacaoServiceImp(repo,base());
 @Test void corteNaoConsultaLoteAindaPlanejado(){
  var lote=mock(LoteCorte.class);when(lote.status()).thenReturn("PLANEJADO");when(repo.lote(1,false)).thenReturn(lote);
  assertEquals(404,assertThrows(AuthException.class,()->service.lote(1,true)).status());
 }
 @Test void corteFinalizaSomenteSeJaEstiverEmCorte(){
  var lote=mock(LoteCorte.class);when(lote.status()).thenReturn("EM_CORTE");when(lote.versao()).thenReturn(2L);when(repo.lote(1,true)).thenReturn(lote);
  service.transicao(1,2,"finalizar");verify(repo).status(1,"CORTADO");
 }
 @Test void versaoAntigaNaoAlteraEstado(){
  var lote=mock(LoteCorte.class);when(lote.versao()).thenReturn(2L);when(repo.lote(1,true)).thenReturn(lote);
  assertThrows(AuthException.class,()->service.transicao(1,1,"iniciar"));verify(repo,never()).status(anyLong(),anyString());
 }
 @Test void planejamentoEnviaFichaERevisaoCorretasParaCongelamento(){
  var ficha=FabricacaoTest.ficha("X","Modelo Branco",0,FabricacaoTest.painel(UUID.randomUUID(),"LAT",new BigDecimal("2200")));
  var modelo=new ModeloGuardaRoupa(2,4,ficha,List.of());when(repo.modelo(2,true)).thenReturn(modelo);
  var dto=new NovoLoteCorte(2,150,LocalDate.now().plusDays(1),"Planejamento",UUID.randomUUID());
  when(repo.criarLote(eq(dto),eq(modelo),anyLong(),any())).thenReturn(1L);
  service.criarLote(dto);verify(repo).criarLote(dto,modelo,7,"Operador");
 }
}
