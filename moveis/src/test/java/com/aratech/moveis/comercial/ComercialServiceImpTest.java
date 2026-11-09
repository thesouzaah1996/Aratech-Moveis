package com.aratech.moveis.comercial;

import com.aratech.moveis.comercial.pedidos.dto.*;
import com.aratech.moveis.comercial.pedidos.repository.ComercialRepository;
import com.aratech.moveis.comercial.pedidos.service.imp.ComercialServiceImp;
import com.aratech.moveis.financeiro.titulos.service.FinanceiroService;
import com.aratech.moveis.pcp.producao.service.ProducaoService;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComercialServiceImpTest {
 final ComercialRepository repo=mock(ComercialRepository.class);
 final FinanceiroService financeiro=mock(FinanceiroService.class);
 final ProducaoService producao=mock(ProducaoService.class);
 final ComercialServiceImp service=new ComercialServiceImp(base(),financeiro,repo,producao);
 Venda venda(String status){return new Venda(1L,"Loja","Modelo Branco",150,new BigDecimal("1800.50"),LocalDate.now().plusDays(10),LocalDate.now().plusDays(30),status,0);}
 @Test void confirmarProvisionaQuantidadeEValorDaVenda(){
  var v=venda("RASCUNHO");when(repo.bloquearPedidoPorId(1L)).thenReturn(v);
  service.vendaAcao(1,"confirmar",comando());
  verify(producao).criarEncomenda(1,"Modelo Branco",150,v.entrega());
  verify(financeiro).integrar("VENDA",1,"RECEBER","Pedido de venda #1","Loja",new BigDecimal("270075.00"),v.vencimento());
  verify(repo).atualizarPedidoComercialConfirmado(1);
 }
 @Test void vendaJaEmProducaoNaoConfirmaNemCancela(){
  when(repo.bloquearPedidoPorId(1L)).thenReturn(venda("EM_PRODUCAO"));
  assertEquals(409,assertThrows(AuthException.class,()->service.vendaAcao(1,"confirmar",comando())).status());
  assertThrows(AuthException.class,()->service.vendaAcao(1,"cancelar",comando()));
  verifyNoInteractions(financeiro,producao);
 }
 @Test void cancelarConfirmadaSincronizaOrdemETitulo(){
  when(repo.bloquearPedidoPorId(1L)).thenReturn(venda("CONFIRMADO"));
  service.vendaAcao(1,"cancelar",comando());
  var ordem=inOrder(producao,financeiro,repo);
  ordem.verify(producao).cancelarEncomenda(1);ordem.verify(financeiro).cancelarOrigem("VENDA",1);ordem.verify(repo).atualizarPedidoComercialCancelado(1);
 }
 @Test void entregaPassadaETotalAcimaDoLimiteNaoPersistem(){
  assertThrows(AuthException.class,()->service.venda(null,new NovaVenda("Loja","Modelo",1,BigDecimal.ONE,LocalDate.now().minusDays(1),LocalDate.now(),UUID.randomUUID(),0)));
  assertThrows(AuthException.class,()->service.venda(null,new NovaVenda("Loja","Modelo",1000000000,new BigDecimal("999999.99"),LocalDate.now().plusDays(1),LocalDate.now(),UUID.randomUUID(),0)));
  verifyNoInteractions(repo,financeiro,producao);
 }
}
