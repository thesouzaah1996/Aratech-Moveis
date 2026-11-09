package com.aratech.moveis.security;

import com.aratech.moveis.security.repository.PerfilUsuarioRepository;
import com.aratech.moveis.security.service.imp.PerfilUsuarioServiceImp;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static com.aratech.moveis.testsupport.OperacaoUnit.usuario;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerfilUsuarioServiceImpTest {
 final PerfilUsuarioRepository repo=mock(PerfilUsuarioRepository.class);
 final PerfilUsuarioServiceImp service=new PerfilUsuarioServiceImp(repo);
 @Test void meuPerfilNaoExibeDadosBancariosOuCpfMesmoSeConsultaOsRetornar(){
  when(repo.consultarFuncionario(7L)).thenReturn(List.of(Map.of("telefone","1130000000","celular","11999999999","cargo","Operador","setor","PCP","ativo",true,"admissao",LocalDate.of(2020,1,1),"contrato","CLT","cpf","12345678900","conta","1234")));
  var dados=service.meuPerfil(usuario());assertFalse(dados.containsKey("cpf"));assertFalse(dados.containsKey("conta"));assertEquals("2020-01-01",dados.get("dataAdmissao"));assertEquals("7",dados.get("matricula"));
 }
 @Test void funcionarioRemovidoNaoRetornaPerfilParcial(){assertEquals(404,assertThrows(AuthException.class,()->service.meuPerfil(usuario())).status());}
}
