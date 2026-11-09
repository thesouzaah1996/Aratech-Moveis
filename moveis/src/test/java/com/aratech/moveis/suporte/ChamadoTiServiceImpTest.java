package com.aratech.moveis.suporte;

import com.aratech.moveis.suporte.chamados.dto.ChamadoTiDTO;
import com.aratech.moveis.suporte.chamados.enums.*;
import com.aratech.moveis.suporte.chamados.exception.ChamadoTiException;
import com.aratech.moveis.suporte.chamados.model.ChamadoTi;
import com.aratech.moveis.suporte.chamados.repository.ChamadoTiRepository;
import com.aratech.moveis.suporte.chamados.service.imp.ChamadoTiServiceImp;
import com.aratech.moveis.suporte.chamados.validation.ChamadoTiValidator;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChamadoTiServiceImpTest {
 final ChamadoTiRepository repo=mock(ChamadoTiRepository.class);
 final ChamadoTiServiceImp service=new ChamadoTiServiceImp(repo,new ChamadoTiValidator());
 ChamadoTiDTO dto(UUID chave,String titulo){return new ChamadoTiDTO(TipoChamadoTi.HARDWARE.toString(),PrioridadeTi.MEDIA.toString(),"PCP",titulo,"Operador","PCP","11999999999","Terminal sem imagem","Cabos conferidos",chave);}
 ChamadoTi existente(UUID chave){return ChamadoTi.builder().id(1L).numero("TI-2026-0001").tipo(TipoChamadoTi.HARDWARE).prioridade(PrioridadeTi.MEDIA).moduloAfetado("PCP").titulo("Terminal sem imagem").solicitante("Operador").setor("PCP").contato("11999999999").descricao("Terminal sem imagem").tentativasSolucao("Cabos conferidos").chaveOperacao(chave).dataAbertura(Instant.now()).build();}
 @Test void reenvioNormalizadoNaoCriaOutroChamado(){
  var chave=UUID.randomUUID();when(repo.findByChaveOperacao(chave)).thenReturn(Optional.of(existente(chave)));
  assertEquals(201,service.abrir(dto(chave," Terminal   sem imagem ")).getStatus());verify(repo,never()).saveAndFlush(any());
 }
 @Test void mesmaChaveComDadosDiferentesEhConflito(){
  var chave=UUID.randomUUID();when(repo.findByChaveOperacao(chave)).thenReturn(Optional.of(existente(chave)));
  assertThrows(ChamadoTiException.class,()->service.abrir(dto(chave,"Outro terminal")));verify(repo,never()).saveAndFlush(any());
 }
 @Test void dadosInvalidosNaoConsultamPersistencia(){assertThrows(ChamadoTiException.class,()->service.abrir(dto(UUID.randomUUID(),"a")));verifyNoInteractions(repo);}
 @Test void chamadoNovoRecebeEstadoAbertoEChaveOriginal(){
  when(repo.saveAndFlush(any())).thenAnswer(i->{ChamadoTi c=i.getArgument(0);c.setId(1L);c.definirNumero();return c;});var chave=UUID.randomUUID();
  service.abrir(dto(chave,"Terminal sem imagem"));verify(repo).saveAndFlush(argThat(c->c.getStatus()==StatusChamadoTi.ABERTO&&c.getChaveOperacao().equals(chave)&&c.getDataAbertura()!=null));
 }
}
