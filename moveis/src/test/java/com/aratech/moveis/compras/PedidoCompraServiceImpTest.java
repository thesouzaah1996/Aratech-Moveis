package com.aratech.moveis.compras;

import com.aratech.moveis.compras.pedidos.dto.*;
import com.aratech.moveis.compras.pedidos.repository.PedidoCompraRepository;
import com.aratech.moveis.compras.pedidos.service.imp.PedidoCompraServiceImp;
import com.aratech.moveis.financeiro.titulos.service.FinanceiroService;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoCompraServiceImpTest {
 final PedidoCompraRepository repo=mock(PedidoCompraRepository.class);
 final FinanceiroService financeiro=mock(FinanceiroService.class);
 final PedidoCompraServiceImp service=new PedidoCompraServiceImp(repo,financeiro);
 final UsuarioSessao comprador=new UsuarioSessao(7L,"Comprador","compras@aratechmoveis",false,0,Instant.now(),Set.of(),Set.of("COMPRAS_PEDIDOS.FINALIZAR"));
 Pedido pedido(String estado){return new Pedido(1L,"PC-1","COMPRAS","MANUAL",null,null,"Dobradiça","DOB",new BigDecimal("8"),"un","Fornecedor",new BigDecimal("4.50"),LocalDate.now(),LocalDate.now().plusDays(2),estado,null,"PCP",null,null,0,LocalDate.now().plusDays(30));}
 @Test void usuarioSemPermissaoNaoDecide(){assertThrows(AuthException.class,()->service.decidir(1L,"aprovar",new Decisao(null,0),usuario()));verifyNoInteractions(repo,financeiro);}
 @Test void receberGeraProvisaoDeQuantidadeVezesPreco(){
  when(repo.buscarPorId(true,1L)).thenReturn(List.of(pedido("Aprovado")));
  when(repo.buscarPorId(false,1L)).thenReturn(List.of(pedido("Recebido")));
  service.decidir(1L,"receber",new Decisao(null,0),comprador);
  verify(financeiro).integrar("COMPRA",1,"PAGAR","Pedido de compra PC-1","Fornecedor",new BigDecimal("36.00"),LocalDate.now().plusDays(30),comprador);
 }
 @Test void pedidoJaRecebidoOuVersaoAntigaNaoDecideNovamente(){
  when(repo.buscarPorId(true,1L)).thenReturn(List.of(pedido("Recebido")));
  assertThrows(AuthException.class,()->service.decidir(1L,"receber",new Decisao(null,0),comprador));
  assertThrows(AuthException.class,()->service.decidir(1L,"aprovar",new Decisao(null,1),comprador));verifyNoInteractions(financeiro);
 }
 @Test void saldoNoMinimoSolicitaUmaUnidadeEReenvioSoAjustaQuantidade(){
  when(repo.bloquearItemDeEstoque(true,2L)).thenReturn(List.of(Map.of("nome","Dobradiça","sku","DOB","quantidade",10)));
  service.minimo("ALMOXARIFADO",2L,BigDecimal.TEN);
  verify(repo).inserirReposicaoSeNecessaria("ALMOXARIFADO",2L,"Dobradiça","DOB",BigDecimal.ONE,"un",null,"ALMOXARIFADO",2L);
  when(repo.bloquearMonitoramento("ALMOXARIFADO",2L)).thenReturn(true);
  service.minimo("ALMOXARIFADO",2L,BigDecimal.TEN);
  verify(repo).atualizarQuantidadeDaReposicao(BigDecimal.ONE,"ALMOXARIFADO",2L,BigDecimal.ONE);
  verify(repo,times(1)).inserirReposicaoSeNecessaria(any(),any(),any(),any(),any(),any(),any(),any(),any());
 }
 @Test void minimoFracionarioDoAlmoxarifadoNaoPersiste(){
  assertThrows(AuthException.class,()->service.minimo("ALMOXARIFADO",2L,new BigDecimal("0.5")));verifyNoInteractions(repo);
 }
}
