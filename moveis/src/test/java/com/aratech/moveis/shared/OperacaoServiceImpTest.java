package com.aratech.moveis.shared;

import com.aratech.moveis.shared.operacao.repository.OperacaoRepository;
import com.aratech.moveis.shared.operacao.model.ComandoPersistido;
import com.aratech.moveis.shared.operacao.service.imp.OperacaoServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import java.util.concurrent.atomic.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.usuario;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OperacaoServiceImpTest {
 final OperacaoRepository repo=mock(OperacaoRepository.class);
 final OperacaoServiceImp service=new OperacaoServiceImp(repo,JsonMapper.builder().build());
 final AtomicReference<ComandoPersistido> persistido=new AtomicReference<>();
 @BeforeEach void preparar(){
  SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(usuario(),null,List.of()));
  when(repo.inserirComando(any(),any(),any())).thenAnswer(i->{persistido.compareAndSet(null,new ComandoPersistido(i.getArgument(1),i.getArgument(2),null));return 1;});
  when(repo.bloquearComando(any())).thenAnswer(i->persistido.get());
  when(repo.atualizarComando(any(),any())).thenAnswer(i->{var p=persistido.get();persistido.set(new ComandoPersistido(p.escopo(),p.assinatura(),i.getArgument(0)));return 1;});
 }
 @AfterEach void limpar(){SecurityContextHolder.clearContext();}
 @Test void reenvioDevolveRespostaPersistidaSemReexecutarEfeito(){
  var chave=UUID.randomUUID();var chamadas=new AtomicInteger();
  assertEquals("LP-1",service.comando(chave,"lote","dados",String.class,()->{chamadas.incrementAndGet();return "LP-1";}));
  assertEquals("LP-1",service.comando(chave,"lote","dados",String.class,()->{fail("Não deveria repetir o efeito");return "";}));assertEquals(1,chamadas.get());
 }
 @Test void mesmaChaveComDadosOuEscopoDiferentesEhConflito(){
  var chave=UUID.randomUUID();service.comando(chave,"lote","dados",String.class,()->"LP-1");
  assertEquals(409,assertThrows(AuthException.class,()->service.comando(chave,"lote","alterado",String.class,()->"LP-2")).status());
  assertThrows(AuthException.class,()->service.comando(chave,"outro","dados",String.class,()->"LP-2"));
 }
 @Test void falhaDoComandoNaoGravaRespostaDeSucesso(){
  assertThrows(IllegalStateException.class,()->service.comando(UUID.randomUUID(),"lote","dados",String.class,()->{throw new IllegalStateException("Falha de estoque");}));
  verify(repo,never()).atualizarComando(any(),any());
 }
 @Test void versaoMotivoEAutorSaoObrigatorios(){
  assertThrows(AuthException.class,()->service.versao(2,1L));assertThrows(AuthException.class,()->service.versao(2,null));
  assertThrows(AuthException.class,()->service.motivo("  "));assertEquals("Conferido",service.motivo(" Conferido "));
  SecurityContextHolder.clearContext();assertEquals(401,assertThrows(AuthException.class,service::autor).status());
 }
}
