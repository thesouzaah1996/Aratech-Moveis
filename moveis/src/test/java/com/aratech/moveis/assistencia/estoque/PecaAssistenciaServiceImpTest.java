package com.aratech.moveis.assistencia.estoque;

import com.aratech.moveis.assistencia.estoque.dto.MovimentoAssistenciaDTO;
import com.aratech.moveis.assistencia.estoque.enums.TipoMovimentoAssistencia;
import com.aratech.moveis.assistencia.estoque.exception.PecaAssistenciaException;
import com.aratech.moveis.assistencia.estoque.model.*;
import com.aratech.moveis.assistencia.estoque.repository.*;
import com.aratech.moveis.assistencia.estoque.service.imp.PecaAssistenciaServiceImp;
import com.aratech.moveis.assistencia.estoque.validation.PecaAssistenciaValidator;
import jakarta.validation.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PecaAssistenciaServiceImpTest {
 static ValidatorFactory factory;
 final PecaAssistenciaRepository repo=mock(PecaAssistenciaRepository.class);
 final MovimentoAssistenciaRepository movimentos=mock(MovimentoAssistenciaRepository.class);
 PecaAssistenciaServiceImp service;PecaAssistencia peca;
 @BeforeAll static void abrir(){factory=Validation.buildDefaultValidatorFactory();}
 @AfterAll static void fechar(){factory.close();}
 @BeforeEach void preparar(){
  service=new PecaAssistenciaServiceImp(repo,movimentos,new PecaAssistenciaValidator(factory.getValidator()));
  peca=PecaAssistencia.builder().id(1L).nome("Cola").codigo("COLA").modelo("Modelo Branco").quantidadeMinima(BigDecimal.ONE).quantidade(new BigDecimal("2.500")).unidade("kg").localizacao("A1").descricao("").criadoEm(Instant.now()).build();
  when(repo.findByIdForUpdate(1L)).thenReturn(Optional.of(peca));when(repo.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));
 }
 MovimentoAssistenciaDTO dto(String quantidade){return new MovimentoAssistenciaDTO(new BigDecimal(quantidade),UUID.randomUUID(),"Conferência de material");}
 @Test void saidaFracionariaPreservaPrecisaoESaldosNoHistorico(){
  service.saida(1L,dto("0.125"));assertEquals(new BigDecimal("2.375"),peca.getQuantidade());
  verify(movimentos).saveAndFlush(argThat(m->m.getTipo()==TipoMovimentoAssistencia.SAIDA&&m.getSaldoAnterior().compareTo(new BigDecimal("2.500"))==0&&m.getSaldoPosterior().compareTo(new BigDecimal("2.375"))==0));
 }
 @Test void estoqueInsuficienteNaoAlteraSaldoNemHistorico(){
  assertThrows(PecaAssistenciaException.class,()->service.saida(1L,dto("3")));assertEquals(new BigDecimal("2.500"),peca.getQuantidade());verify(repo,never()).saveAndFlush(any());verify(movimentos,never()).saveAndFlush(any());
 }
 @Test void reenvioIdenticoNaoMovimentaNovamente(){
  var d=dto("0.125");when(movimentos.findByChaveOperacao(d.getChaveOperacao())).thenReturn(Optional.of(MovimentoAssistencia.builder().peca(peca).tipo(TipoMovimentoAssistencia.SAIDA).quantidade(d.getQuantidade()).motivo(d.getMotivo()).build()));
  service.saida(1L,d);verify(repo,never()).saveAndFlush(any());verify(movimentos,never()).saveAndFlush(any());
 }
 @Test void mesmaChaveComQuantidadeDiferenteEhConflito(){
  var d=dto("0.125");when(movimentos.findByChaveOperacao(d.getChaveOperacao())).thenReturn(Optional.of(MovimentoAssistencia.builder().peca(peca).tipo(TipoMovimentoAssistencia.SAIDA).quantidade(BigDecimal.ONE).motivo(d.getMotivo()).build()));
  assertThrows(PecaAssistenciaException.class,()->service.saida(1L,d));verify(repo,never()).saveAndFlush(any());
 }
 @Test void naoArquivaPecaComSaldo(){assertThrows(PecaAssistenciaException.class,()->service.arquivar(1L));assertTrue(peca.isAtivo());}
}
