package com.aratech.moveis.pcp;

import com.aratech.moveis.pcp.requisicoes.dto.Requisicao;
import com.aratech.moveis.pcp.requisicoes.repository.RequisicaoRepository;
import com.aratech.moveis.pcp.requisicoes.service.imp.RequisicaoServiceImp;
import com.aratech.moveis.almoxarifado.produtos.service.ProdutoService;
import com.aratech.moveis.almoxarifado.produtos.dto.MovimentacaoDTO;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequisicaoServiceImpTest {
 final RequisicaoRepository repo=mock(RequisicaoRepository.class);
 final ProdutoService estoque=mock(ProdutoService.class);
 final RequisicaoServiceImp service=new RequisicaoServiceImp(base(),repo,estoque);
 void estado(String estado){when(repo.bloquearRequisicao(1)).thenReturn(new Requisicao(1L,2L,"Modelo",3L,"Dobradiça",1200,"Produção",estado,null,0));}
 @Test void atenderBaixaTotalComMesmaChaveDoComando(){
  estado("PENDENTE");when(repo.bloquearOrdemAberta(2L)).thenReturn(List.of(Map.of("id",2L)));var cmd=comando();
  service.acao(1,"atender",cmd);
  var movimento=org.mockito.ArgumentCaptor.forClass(MovimentacaoDTO.class);verify(estoque).saida(eq(3L),movimento.capture());
  assertEquals(new BigDecimal("1200"),movimento.getValue().getQuantidade());assertEquals(cmd.chaveOperacao(),movimento.getValue().getChaveOperacao());
  verify(repo).registrarDecisao("ATENDIDA",cmd.motivo(),1);
 }
 @Test void requisicaoDecididaNaoBaixaDeNovo(){estado("ATENDIDA");assertThrows(AuthException.class,()->service.acao(1,"atender",comando()));verifyNoInteractions(estoque);}
 @Test void devolucaoExigeOrdemAindaPlanejada(){estado("ATENDIDA");assertThrows(AuthException.class,()->service.acao(1,"devolver",comando()));verifyNoInteractions(estoque);}
 @Test void devolucaoValidaRetornaMaterialSemNovaSaida(){
  estado("ATENDIDA");when(repo.bloquearOrdemPlanejada(2L)).thenReturn(List.of(Map.of("id",2L)));
  service.acao(1,"devolver",comando());verify(estoque).entrada(eq(3L),argThat(m->m.getQuantidade().compareTo(new BigDecimal("1200"))==0));
  verify(estoque,never()).saida(anyLong(),any());verify(repo).registrarDecisao(eq("DEVOLVIDA"),anyString(),eq(1L));
 }
}
