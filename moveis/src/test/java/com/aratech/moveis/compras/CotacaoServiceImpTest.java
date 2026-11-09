package com.aratech.moveis.compras;

import com.aratech.moveis.compras.cotacoes.dto.*;
import com.aratech.moveis.compras.cotacoes.repository.CotacaoRepository;
import com.aratech.moveis.compras.cotacoes.service.imp.CotacaoServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CotacaoServiceImpTest {
 final CotacaoRepository repo=mock(CotacaoRepository.class);
 final CotacaoServiceImp service=new CotacaoServiceImp(base(),repo);
 NovaCotacao proposta(String segundo,BigDecimal valor){return new NovaCotacao(2L,"Fornecedor A",BigDecimal.TEN,segundo,valor,null,null,UUID.randomUUID(),0);}
 Cotacao cotacao(){return new Cotacao(1L,2L,"Dobradiça",BigDecimal.TEN,"Fornecedor A",BigDecimal.TEN,"Fornecedor B",new BigDecimal("8.50"),null,null,null,"ABERTA",0);}
 @Test void rejeitaFornecedorRepetidoMesmoComCaixaEEspacos(){
  assertThrows(AuthException.class,()->service.criar(proposta(" FORNECEDOR A ",BigDecimal.ONE)));verifyNoInteractions(repo);
 }
 @Test void rejeitaPropostaOpcionalSemPrecoOuFornecedor(){
  assertThrows(AuthException.class,()->service.criar(proposta("Fornecedor B",null)));
  assertThrows(AuthException.class,()->service.criar(proposta(null,BigDecimal.ONE)));verifyNoInteractions(repo);
 }
 @Test void escolherAtualizaFornecedorEPrecoSemAprovarPedido(){
  when(repo.bloquearCotacao(1)).thenReturn(cotacao());when(repo.bloquearPedidoAberto(2L)).thenReturn(List.of(Map.of("id",2L)));
  service.acao(1,"escolher",new Comando(UUID.randomUUID(),0L,"Melhor proposta",null,null,2));
  verify(repo).definirFornecedorEPreco("Fornecedor B",new BigDecimal("8.50"),2L);verify(repo).registrarEscolha(2,1);
 }
 @Test void pedidoJaDecididoNaoTrocaFornecedor(){
  when(repo.bloquearCotacao(1)).thenReturn(cotacao());
  assertThrows(AuthException.class,()->service.acao(1,"escolher",new Comando(UUID.randomUUID(),0L,"Melhor proposta",null,null,1)));
  verify(repo,never()).definirFornecedorEPreco(any(),any(),anyLong());verify(repo,never()).registrarEscolha(anyInt(),anyLong());
 }
}
