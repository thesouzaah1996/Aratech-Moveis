package com.aratech.moveis.pcp;

import com.aratech.moveis.pcp.producao.dto.*;
import com.aratech.moveis.pcp.producao.repository.ProducaoRepository;
import com.aratech.moveis.pcp.producao.service.imp.ProducaoServiceImp;
import com.aratech.moveis.pcp.lotes.LotesProducaoService;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProducaoServiceImpTest {
 final ProducaoRepository repo=mock(ProducaoRepository.class);
 final LotesProducaoService lotes=mock(LotesProducaoService.class);
 final ProducaoServiceImp service=new ProducaoServiceImp(base(),repo,lotes);
 Ordem ordem(String status){return new Ordem(1L,"Modelo",150,LocalDate.now(),LocalDate.now().plusDays(1),"PCP",2L,"Linha",new BigDecimal("16"),3L,status,null,null,0);}
 @Test void excederCapacidadeEmQualquerDiaImpedeSalvar(){
  when(repo.bloquearLinhaPorId(2L)).thenReturn(new Linha(2L,"Linha",new BigDecimal("8"),true,0));
  when(repo.listarReservasDeCapacidade(anyLong(),isNull(),isNull(),any(),any())).thenReturn(List.of(new com.aratech.moveis.pcp.producao.model.ReservaCapacidade(LocalDate.now(),LocalDate.now().plusDays(1),new BigDecimal("16"))));
  var dto=new NovaOrdem("Modelo",150,LocalDate.now(),LocalDate.now().plusDays(1),"PCP",2L,BigDecimal.ONE,UUID.randomUUID(),0);
  assertThrows(AuthException.class,()->service.salvar(null,dto));
  verify(repo,never()).inserirOrdem(any(),anyInt(),any(),any(),any(),any(),any());
 }
 @Test void requisicaoPendenteImpedeIniciar(){
  when(repo.bloquearOrdemPorId(1L)).thenReturn(new EstadoOrdem(1L,0,"PLANEJADA",3L));
  when(repo.buscarOrdem(1)).thenReturn(ordem("PLANEJADA"));when(repo.contarRequisicoesPendentes(1)).thenReturn(1L);
  assertThrows(AuthException.class,()->service.acao(1,"iniciar",comando()));verify(repo,never()).atualizarOrdemEmProducao(1);
 }
 @Test void conclusaoDependeDosLotesESincronizaComercial(){
  when(repo.bloquearOrdemPorId(1L)).thenReturn(new EstadoOrdem(1L,0,"EM_PRODUCAO",3L));when(repo.buscarOrdem(1)).thenReturn(ordem("EM_PRODUCAO"));
  service.acao(1,"concluir",comando());
  var sequencia=inOrder(lotes,repo);sequencia.verify(lotes).exigirOrigemConcluida(1);sequencia.verify(repo).atualizarOrdemFinalizada(1);sequencia.verify(repo).atualizarPedidoComercialPronto(3L);
 }
 @Test void lotePendenteBloqueiaConclusaoSemAtualizarOrigem(){
  when(repo.bloquearOrdemPorId(1L)).thenReturn(new EstadoOrdem(1L,0,"EM_PRODUCAO",3L));when(repo.buscarOrdem(1)).thenReturn(ordem("EM_PRODUCAO"));
  doThrow(new AuthException(409,"Lote pendente")).when(lotes).exigirOrigemConcluida(1);
  assertThrows(AuthException.class,()->service.acao(1,"concluir",comando()));verify(repo,never()).atualizarOrdemFinalizada(1);
 }
 @Test void encomendaComMaterialEntregueNaoCancela(){
  when(repo.bloquearOrdensDaEncomenda(3)).thenReturn(List.of("PLANEJADA"));when(repo.contarMateriaisEntreguesDaEncomenda(3)).thenReturn(1L);
  assertThrows(AuthException.class,()->service.cancelarEncomenda(3));verify(repo,never()).cancelarOrdensDaEncomenda(3);
 }
}
