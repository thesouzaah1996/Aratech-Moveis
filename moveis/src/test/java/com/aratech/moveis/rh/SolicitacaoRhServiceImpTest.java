package com.aratech.moveis.rh;

import com.aratech.moveis.rh.solicitacoes.dto.*;
import com.aratech.moveis.rh.solicitacoes.repository.SolicitacaoRhRepository;
import com.aratech.moveis.rh.solicitacoes.service.imp.SolicitacaoRhServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitacaoRhServiceImpTest {
 final SolicitacaoRhRepository repo=mock(SolicitacaoRhRepository.class);
 final SolicitacaoRhServiceImp service=new SolicitacaoRhServiceImp(base(),repo);
 void estado(String estado){when(repo.bloquearSolicitacao(1)).thenReturn(new Solicitacao(1L,7L,"Operador","DECLARACAO","Vínculo",estado,null,Instant.now(),0));}
 @Test void inativoNaoAbreSolicitacao(){
  assertThrows(AuthException.class,()->service.solicitar(new NovaSolicitacao(7L,"DECLARACAO","Vínculo",UUID.randomUUID(),0)));
  verify(repo,never()).inserirSolicitacao(any(),any(),any());
 }
 @Test void iniciarMudaPendenteParaAnalise(){estado("PENDENTE");var cmd=comando();service.solicitacaoAcao(1,"iniciar",cmd);verify(repo).registrarResposta("EM_ANALISE",cmd.motivo(),1);}
 @Test void aprovaEmAnaliseMasNaoReabreDecidida(){
  estado("EM_ANALISE");var cmd=comando();service.solicitacaoAcao(1,"aprovar",cmd);verify(repo).registrarResposta("APROVADO",cmd.motivo(),1);
  estado("APROVADO");assertThrows(AuthException.class,()->service.solicitacaoAcao(1,"iniciar",comando()));verify(repo,times(1)).registrarResposta(any(),any(),anyLong());
 }
}
