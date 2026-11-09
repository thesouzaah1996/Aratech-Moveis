package com.aratech.moveis.security;

import com.aratech.moveis.security.dto.*;
import com.aratech.moveis.security.model.DadosConta;
import com.aratech.moveis.security.repository.ContasRepository;
import com.aratech.moveis.security.service.imp.ContasServiceImp;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContasServiceImpTest {
 final ContasRepository repo=mock(ContasRepository.class);
 final ContasServiceImp service=new ContasServiceImp(repo);
 @Test void trocaObrigatoriaRemovePermissoesMesmoDoAdministrador(){
  when(repo.consultarFuncionarioAtivo(7L)).thenReturn(List.of(new DadosConta(7L,"Operador","op@aratechmoveis",true,2,true)));
  var u=service.carregar(7L,Instant.now());assertTrue(u.trocarSenha());assertTrue(u.permissoes().isEmpty());verify(repo,never()).consultarPermissao(any());
 }
 @Test void permissoesDaSessaoNaoPodemSerModificadas(){
  when(repo.consultarFuncionarioAtivo(7L)).thenReturn(List.of(new DadosConta(7L,"Operador","op@aratechmoveis",false,2,false)));
  when(repo.consultarPermissao(7L)).thenReturn(List.of("PCP_LOTES.CONSULTAR"));
  var u=service.carregar(7L,Instant.now());assertEquals(Set.of("PCP_LOTES.CONSULTAR"),u.permissoes());assertThrows(UnsupportedOperationException.class,()->u.permissoes().add("ACESSOS.GERENCIAR"));
 }
 @Test void funcionarioInativoNaoCarregaSessao(){assertThrows(BadCredentialsException.class,()->service.carregar(7L,Instant.now()));}
 @Test void credencialTecnicaTemPrioridadeSemConsultarContaPessoal(){
  var c=new Credencial(0L,"hash");when(repo.consultarAdministrador("admin@aratechmoveis")).thenReturn(List.of(c));
  assertEquals(Optional.of(c),service.credencial("admin@aratechmoveis"));verify(repo,never()).consultarConta(any());
 }
}
