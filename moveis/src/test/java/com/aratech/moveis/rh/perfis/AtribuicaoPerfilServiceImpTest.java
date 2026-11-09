package com.aratech.moveis.rh.perfis;

import com.aratech.moveis.rh.perfis.dto.*;
import com.aratech.moveis.rh.perfis.exception.PerfilException;
import com.aratech.moveis.rh.perfis.repository.AtribuicaoPerfilRepository;
import com.aratech.moveis.rh.perfis.service.imp.AtribuicaoPerfilServiceImp;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AtribuicaoPerfilServiceImpTest {
 final AtribuicaoPerfilRepository repo=mock(AtribuicaoPerfilRepository.class);
 final AtribuicaoPerfilServiceImp service=new AtribuicaoPerfilServiceImp(repo);
 @Test void perfilInexistenteNaoCriaCadastroAutomatico(){
  when(repo.bloquearFuncionario(7L)).thenReturn(List.of(new UsuarioPerfilDTO(7L,"Operador","op@aratechmoveis","PCP",List.of())));
  assertThrows(PerfilException.class,()->service.atribuir(7L,"Perfil novo"));verify(repo,never()).inserirVinculo(any(),any());
 }
 @Test void normalizaNomeERepeticaoDeVinculoRetornaPerfisAtuais(){
  var perfil=new PerfilResumoDTO(2L,"Operador PCP");
  when(repo.bloquearFuncionario(7L)).thenReturn(List.of(new UsuarioPerfilDTO(7L,"Operador","op@aratechmoveis","PCP",List.of())));
  when(repo.buscarPerfilPorNome("Operador PCP")).thenReturn(List.of(perfil));when(repo.listarPerfisDoUsuario(7L)).thenReturn(List.of(perfil));
  when(repo.inserirVinculo(7L,2L)).thenReturn(0);
  assertEquals(List.of(perfil),service.atribuir(7L," Operador   PCP ").perfis());
 }
 @Test void buscaRejeitaControleEPaginacaoComOverflow(){
  assertThrows(PerfilException.class,()->service.buscar("nome\ninterno",0,12));assertThrows(PerfilException.class,()->service.buscar("",Integer.MAX_VALUE,100));verifyNoInteractions(repo);
 }
}
