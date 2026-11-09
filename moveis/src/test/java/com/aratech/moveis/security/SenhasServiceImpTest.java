package com.aratech.moveis.security;

import com.aratech.moveis.security.repository.SenhasRepository;
import com.aratech.moveis.security.service.LimiteLoginService;
import com.aratech.moveis.security.service.imp.SenhasServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.usuario;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SenhasServiceImpTest {
 final SenhasRepository repo=mock(SenhasRepository.class);
 final PasswordEncoder encoder=mock(PasswordEncoder.class);
 final LimiteLoginService limite=mock(LimiteLoginService.class);
 final SenhasServiceImp service=new SenhasServiceImp(repo,encoder,limite);
 @Test void acessoExistenteNaoTemSenhaSubstituida(){
  when(repo.buscarEmailDoFuncionarioAtivoComBloqueio(7L)).thenReturn(List.of("op@aratechmoveis"));when(repo.existeConta(7L)).thenReturn(true);
  assertEquals(409,assertThrows(AuthException.class,()->service.criarAcesso(7L)).status());verifyNoInteractions(encoder);
 }
 @Test void credencialTemporariaEhArmazenadaSomenteComoHash(){
  when(repo.buscarEmailDoFuncionarioAtivoComBloqueio(7L)).thenReturn(List.of("op@aratechmoveis"));when(encoder.encode(any())).thenReturn("hash-seguro");
  var temporaria=service.criarAcesso(7L);assertEquals(24,temporaria.length());verify(encoder).encode(temporaria);verify(repo).inserirConta(7L,"hash-seguro",null);
 }
 @Test void senhaAtualIncorretaNaoAlteraHash(){
  when(repo.buscarHashDaContaComBloqueio(7L)).thenReturn(List.of("hash-antigo"));
  assertThrows(AuthException.class,()->service.alterar(usuario(),"SenhaAtual@123","vT7!qR9#pL2$zK8@"));
  verify(limite).consumir("troca-senha","7",10);verify(repo,never()).alterarSenhaDaConta(any(),any());
 }
 @Test void naoPermiteReutilizarSenhaAtual(){
  assertThrows(AuthException.class,()->service.alterar(usuario(),"igual","igual"));verifyNoInteractions(repo,encoder,limite);
 }
}
