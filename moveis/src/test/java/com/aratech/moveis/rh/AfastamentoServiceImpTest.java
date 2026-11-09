package com.aratech.moveis.rh;

import com.aratech.moveis.rh.afastamentos.dto.*;
import com.aratech.moveis.rh.afastamentos.repository.AfastamentoRepository;
import com.aratech.moveis.rh.afastamentos.service.imp.AfastamentoServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AfastamentoServiceImpTest {
 final AfastamentoRepository repo=mock(AfastamentoRepository.class);
 final AfastamentoServiceImp service=new AfastamentoServiceImp(base(),repo);
 final LocalDate inicio=LocalDate.now().plusDays(5),fim=inicio.plusDays(10);
 NovoAfastamento dto(){return new NovoAfastamento(7L,"FERIAS",inicio,fim,"Férias solicitadas",UUID.randomUUID(),0);}
 @Test void funcionarioInativoNaoSolicita(){assertEquals(404,assertThrows(AuthException.class,()->service.criarAfastamento(dto())).status());verify(repo,never()).inserirAfastamento(any(),any(),any(),any(),any());}
 @Test void periodoSobrepostoNaoPersiste(){
  when(repo.bloquearFuncionarioAtivo(7)).thenReturn(List.of(Map.of("id",7L)));when(repo.contarSobreposicoesAtivas(7L,fim,inicio)).thenReturn(1L);
  assertThrows(AuthException.class,()->service.criarAfastamento(dto()));verify(repo,never()).inserirAfastamento(any(),any(),any(),any(),any());
 }
 @Test void aprovacaoConfereConflitoAntesDeRegistrarDecisao(){
  when(repo.bloquearAfastamento(1)).thenReturn(new Afastamento(1L,7L,"Operador","FERIAS",inicio,fim,"Férias","PENDENTE",null,0));
  when(repo.bloquearFuncionarioAtivo(7)).thenReturn(List.of(Map.of("id",7L)));when(repo.contarAprovadosSobrepostos(7L,1,fim,inicio)).thenReturn(1L);
  assertThrows(AuthException.class,()->service.afastamentoAcao(1,"aprovar",comando()));verify(repo,never()).registrarDecisao(any(),any(),anyLong());
 }
 @Test void periodoValidoEhSalvoParaFuncionarioAtivo(){
  when(repo.bloquearFuncionarioAtivo(7)).thenReturn(List.of(Map.of("id",7L)));when(repo.inserirAfastamento(7L,"FERIAS",inicio,fim,"Férias solicitadas")).thenReturn(1L);
  service.criarAfastamento(dto());verify(repo).buscarAfastamento(1L);
 }
}
