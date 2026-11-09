package com.aratech.moveis.testsupport;

import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.operacao.repository.OperacaoRepository;
import com.aratech.moveis.shared.operacao.service.imp.OperacaoServiceImp;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import static org.mockito.Mockito.*;

/** Isola a persistência do comando; mantém as validações reais de versão, condição e motivo. */
public final class OperacaoUnit {
 private OperacaoUnit() {}
 public static UsuarioSessao usuario(){return new UsuarioSessao(7L,"Operador","operador@aratechmoveis",false,0,Instant.now(),Set.of(),Set.of());}
 public static Comando comando(){return new Comando(UUID.randomUUID(),0L,"Conferência da operação",null,null,null);}
 public static OperacaoServiceImp base(){
  var base=spy(new OperacaoServiceImp(mock(OperacaoRepository.class),JsonMapper.builder().build()));
  doReturn(usuario()).when(base).autor();
  doAnswer(i->((Supplier<?>)i.getArgument(4)).get()).when(base).comando(any(),anyString(),any(),any(),any());
  return base;
 }
}
