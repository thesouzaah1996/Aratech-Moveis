package com.aratech.moveis.administracao;

import com.aratech.moveis.administracao.gestao.dto.*;
import com.aratech.moveis.administracao.gestao.repository.AdministracaoRepository;
import com.aratech.moveis.administracao.gestao.service.imp.AdministracaoServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdministracaoServiceImpTest {
 final AdministracaoRepository repo=mock(AdministracaoRepository.class);
 final com.aratech.moveis.shared.operacao.service.OperacaoService base=base();
 final AdministracaoServiceImp service=new AdministracaoServiceImp(base,repo);
 @Test void atualizaParametroComVersaoAtualERegistraAuditoria(){
  when(repo.bloquearParametro("pcp.antecedencia_dias")).thenReturn(new Parametro("pcp.antecedencia_dias","Prazo",3,2));
  service.parametro("pcp.antecedencia_dias",new ValorParametro(5,UUID.randomUUID(),2L));
  verify(repo).atualizarParametro(5,"pcp.antecedencia_dias");
  verify(base).auditar(eq("ADMINISTRACAO"),eq("ALTERAR"),eq("PARAMETRO"),isNull(),contains("5"));
 }
 @Test void versaoAntigaNaoAlteraParametro(){
  when(repo.bloquearParametro("prazo")).thenReturn(new Parametro("prazo","Prazo",3,2));
  assertThrows(AuthException.class,()->service.parametro("prazo",new ValorParametro(5,UUID.randomUUID(),1L)));
  verify(repo,never()).atualizarParametro(any(),anyString());
 }
 @Test void indicadoresUsamAntecedenciasConfiguradas(){
  doReturn(4).when(base).parametro("pcp.antecedencia_dias");doReturn(7).when(base).parametro("financeiro.antecedencia_dias");
  when(repo.titulosVencidos()).thenReturn(new BigDecimal("120.50"));
  var relatorio=service.relatorio();assertEquals(7,relatorio.size());
  assertTrue(relatorio.stream().anyMatch(i->new BigDecimal("120.50").equals(i.valor())));
  verify(repo).producaoProxima(4);verify(repo).pagamentosProximos(7);
 }
}
