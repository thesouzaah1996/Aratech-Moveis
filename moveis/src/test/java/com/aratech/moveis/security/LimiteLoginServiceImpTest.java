package com.aratech.moveis.security;

import com.aratech.moveis.security.repository.LimiteLoginRepository;
import com.aratech.moveis.security.model.TentativaLogin;
import com.aratech.moveis.security.service.imp.LimiteLoginServiceImp;
import com.aratech.moveis.security.exception.ExcessoTentativas;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LimiteLoginServiceImpTest {
 final LimiteLoginRepository repo=mock(LimiteLoginRepository.class);
 final LimiteLoginServiceImp service=new LimiteLoginServiceImp(repo);
 @Test void limiteAtingidoBloqueiaSemIncrementar(){
  when(repo.bloquearTentativa(any())).thenReturn(new TentativaLogin(Instant.now(),10));
  assertThrows(ExcessoTentativas.class,()->service.consumir("conta","operador@aratechmoveis",10));verify(repo,never()).incrementarTentativas(any());
 }
 @Test void janelaExpiradaReiniciaMesmoAposLimite(){
  when(repo.bloquearTentativa(any())).thenReturn(new TentativaLogin(Instant.now().minus(Duration.ofMinutes(16)),10));
  service.consumir("conta","operador@aratechmoveis",10);verify(repo).reiniciarJanela(any());verify(repo,never()).incrementarTentativas(any());
 }
 @Test void identificadorPersistidoNaoExibeEmail(){
  when(repo.bloquearTentativa(any())).thenReturn(new TentativaLogin(Instant.now(),1));
  service.consumir("conta","operador@aratechmoveis",10);
  verify(repo).incrementarTentativas(argThat(c->c.startsWith("conta:")&&!c.contains("operador")));
 }
}
