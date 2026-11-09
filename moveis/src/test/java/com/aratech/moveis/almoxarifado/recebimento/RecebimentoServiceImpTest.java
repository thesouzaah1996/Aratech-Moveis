package com.aratech.moveis.almoxarifado.recebimento;

import com.aratech.moveis.almoxarifado.recebimento.service.imp.RecebimentoServiceImp;
import com.aratech.moveis.portaria.controleacesso.service.ControleAcessoService;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaResumoDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecebimentoServiceImpTest {
 final ControleAcessoService controle=mock(ControleAcessoService.class);
 final RecebimentoServiceImp service=new RecebimentoServiceImp(controle);
 @Test void autorizacaoFicaRestritaAoAlmoxarifado(){
  var chegada=mock(ChegadaResumoDTO.class);when(controle.autorizarSetor("ALMOXARIFADO",1L)).thenReturn(chegada);
  assertSame(chegada,service.autorizar(1L));verify(controle,never()).autorizarSetor(eq("CARREGAMENTO"),any());
 }
 @Test void falhaDeFinalizacaoDaPortariaNaoEhTratadaComoSucesso(){
  var falha=new IllegalStateException("Entrada ainda não confirmada");when(controle.finalizarSetor("ALMOXARIFADO",1L)).thenThrow(falha);
  assertSame(falha,assertThrows(IllegalStateException.class,()->service.finalizar(1L)));
 }
}
