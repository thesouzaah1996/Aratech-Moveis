package com.aratech.moveis.security;

import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.repository.GestaoAcessoRepository;
import com.aratech.moveis.security.service.imp.GestaoAcessoServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestaoAcessoServiceImpTest {
 final GestaoAcessoRepository repo=mock(GestaoAcessoRepository.class);
 final GestaoAcessoServiceImp service=new GestaoAcessoServiceImp(repo);
 void autenticar(boolean admin,Set<String> permissoes){var u=new UsuarioSessao(7L,"Gestor","gestor@aratechmoveis",admin,0,Instant.now(),Set.of(),permissoes);SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(u,null,List.of()));}
 @AfterEach void limpar(){SecurityContextHolder.clearContext();}
 @Test void gestorNaoConcedePermissaoAcimaDasSuas(){
  autenticar(false,Set.of("ACESSOS.GERENCIAR","PCP_LOTES.CONSULTAR"));when(repo.bloquearPerfilPorId(2L)).thenReturn(List.of(2L));when(repo.existePerfil(2L)).thenReturn(true);
  assertEquals(403,assertThrows(AuthException.class,()->service.salvar(2L,Set.of("FINANCEIRO_PAGAR.CONSULTAR"))).status());verify(repo,never()).removerPermissoesDoPerfil(any());
 }
 @Test void escritaSemConsultaEhInvalidaMesmoParaAdministrador(){
  autenticar(true,Set.of());assertEquals(400,assertThrows(AuthException.class,()->service.salvar(2L,Set.of("PCP_LOTES.ALTERAR"))).status());verifyNoInteractions(repo);
 }
 @Test void ultimoAdministradorNaoPodeSerDesativado(){
  autenticar(true,Set.of());when(repo.administrador(9L)).thenReturn(true);when(repo.contarAdministradoresAtivos()).thenReturn(1L);
  assertEquals(409,assertThrows(AuthException.class,()->service.validarAlteracaoFuncionario(9L,true)).status());
 }
 @Test void consultaDePerfilInexistenteRetorna404(){assertEquals(404,assertThrows(AuthException.class,()->service.consultar(2L)).status());}
}
