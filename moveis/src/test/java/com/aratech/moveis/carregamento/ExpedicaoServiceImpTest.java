package com.aratech.moveis.carregamento;

import com.aratech.moveis.carregamento.expedicao.repository.ExpedicaoRepository;
import com.aratech.moveis.carregamento.expedicao.service.imp.ExpedicaoServiceImp;
import com.aratech.moveis.pcp.producao.dto.*;
import com.aratech.moveis.pcp.producao.service.ProducaoService;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpedicaoServiceImpTest {
 final ExpedicaoRepository repo=mock(ExpedicaoRepository.class);
 final ProducaoService producao=mock(ProducaoService.class);
 final ExpedicaoServiceImp service=new ExpedicaoServiceImp(base(),producao,repo);
 void ordem(String status,Instant saida){
  when(repo.bloquearOrdemParaExpedicao(1)).thenReturn(new EstadoOrdem(1L,0,status,3L));
  when(producao.buscar(1)).thenReturn(new Ordem(1L,"Modelo",150,LocalDate.now(),LocalDate.now(),"PCP",2L,"Linha",BigDecimal.TEN,3L,status,Instant.now(),saida,0));
 }
 Comando cmd(Long caminhao){return new Comando(UUID.randomUUID(),0L,"Conferência de carga",null,caminhao,null);}
 @Test void semCaminhaoOuSemEntradaConfirmadaNaoExpede(){
  ordem("FINALIZADA",null);assertEquals(400,assertThrows(AuthException.class,()->service.expedir(1,cmd(null))).status());
  assertEquals(409,assertThrows(AuthException.class,()->service.expedir(1,cmd(9L))).status());
  verify(repo,never()).registrarExpedicao(any(),anyLong());
 }
 @Test void ordemAbertaOuJaExpedidaNaoSai(){
  ordem("EM_PRODUCAO",null);assertThrows(AuthException.class,()->service.expedir(1,cmd(9L)));
  ordem("FINALIZADA",Instant.now());assertThrows(AuthException.class,()->service.expedir(1,cmd(9L)));
  verify(repo,never()).registrarExpedicao(any(),anyLong());
 }
 @Test void expedirVinculaCaminhaoESincronizaPedido(){
  ordem("FINALIZADA",null);when(repo.bloquearCaminhaoEmAtendimento(9L)).thenReturn(List.of(Map.of("id",9L)));
  service.expedir(1,cmd(9L));verify(repo).registrarExpedicao(9L,1);verify(repo).atualizarPedidoComercialExpedido(3L);
 }
}
