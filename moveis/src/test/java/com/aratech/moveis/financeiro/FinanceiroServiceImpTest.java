package com.aratech.moveis.financeiro;

import com.aratech.moveis.financeiro.titulos.dto.*;
import com.aratech.moveis.financeiro.titulos.repository.FinanceiroRepository;
import com.aratech.moveis.financeiro.titulos.service.imp.FinanceiroServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanceiroServiceImpTest {
 final FinanceiroRepository repo=mock(FinanceiroRepository.class);
 final FinanceiroServiceImp service=new FinanceiroServiceImp(base(),repo);
 Titulo titulo(String origem,String status){return new Titulo(1L,"PAGAR","Compra","Fornecedor",new BigDecimal("100.00"),LocalDate.now().plusDays(10),null,LocalDate.now().minusDays(5),null,origem,2L,status,null,0);}
 @Test void baixaValidaUsaDataReal(){
  when(repo.bloquearTituloPorId(1)).thenReturn(titulo("COMPRA","PENDENTE"));
  service.acao("pagar",1,"baixar",new Comando(UUID.randomUUID(),0L,"Pagamento conferido",LocalDate.now(),null,null));
  verify(repo).atualizarTituloBaixado(LocalDate.now(),1);
 }
 @Test void recusaBaixaFuturaAnteriorAEmissaoEOuJaBaixada(){
  when(repo.bloquearTituloPorId(1)).thenReturn(titulo("MANUAL","PENDENTE"));
  for(var data:List.of(LocalDate.now().plusDays(1),LocalDate.now().minusDays(6)))
   assertEquals(400,assertThrows(AuthException.class,()->service.acao("pagar",1,"baixar",new Comando(UUID.randomUUID(),0L,"Pagamento conferido",data,null,null))).status());
  when(repo.bloquearTituloPorId(1)).thenReturn(titulo("MANUAL","BAIXADO"));
  assertThrows(AuthException.class,()->service.acao("pagar",1,"baixar",comando()));
  verify(repo,never()).atualizarTituloBaixado(any(),anyLong());
 }
 @Test void naoCancelaTituloIntegradoNemOrigemComBaixa(){
  when(repo.bloquearTituloPorId(1)).thenReturn(titulo("COMPRA","PENDENTE"));
  assertThrows(AuthException.class,()->service.acao("pagar",1,"cancelar",comando()));
  when(repo.bloquearTitulosDaOrigem("COMPRA",2)).thenReturn(List.of("BAIXADO"));
  assertThrows(AuthException.class,()->service.cancelarOrigem("COMPRA",2));
  verify(repo,never()).atualizarTituloCancelado(anyLong());verify(repo,never()).cancelarPendentesDaOrigem(anyString(),anyLong());
 }
 @Test void documentoVinculaAoMesmoTitulo(){
  when(repo.bloquearTituloPorId(1)).thenReturn(titulo("COMPRA","PENDENTE"));
  service.documentar("notas",1,new Documento(" NF-123 ",LocalDate.now(),UUID.randomUUID(),0L));
  verify(repo).vincularDocumento("NF-123",LocalDate.now(),1);
  verify(repo,never()).inserirTitulo(any(),any(),any(),any(),any(),any(),any(),any());
 }
 @Test void integracaoRepetidaSemNovoRegistroNaoAuditaOutraProvisao(){
  var base=base();var s=new FinanceiroServiceImp(base,repo);
  when(repo.inserirProvisao(any(),any(),any(),any(),any(),any(),anyLong())).thenReturn(List.of());
  s.integrar("VENDA",2,"RECEBER","Venda","Cliente",BigDecimal.TEN,LocalDate.now(),usuario());
  verify(base,never()).auditar(any(),anyString(),anyString(),anyString(),any(),anyString());
  assertThrows(AuthException.class,()->s.integrar("VENDA",2,"RECEBER","Venda","Cliente",BigDecimal.ZERO,LocalDate.now(),usuario()));
 }
 @Test void caixaValidaPeriodoEIncluiAteOPrimeiroDiaDoMesSeguinte(){
  service.caixa("2026-12",0,12,"");verify(repo).listarCaixa(0,12,"",LocalDate.of(2027,1,1));
  for(String mes:List.of("2026-13","1899-01","2101-01","invalido"))assertThrows(AuthException.class,()->service.caixa(mes,0,12,""));
 }
}
