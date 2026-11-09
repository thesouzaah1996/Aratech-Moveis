package com.aratech.moveis.shared;

import com.aratech.moveis.shared.apoio.repository.ApoioRepository;
import com.aratech.moveis.shared.apoio.service.imp.ApoioServiceImp;
import com.aratech.moveis.shared.dto.Opcao;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApoioServiceImpTest {
 @Test void opcoesDeProdutoNaoMisturamCategoriasComFornecedores(){
  var repo=mock(ApoioRepository.class);when(repo.categorias()).thenReturn(List.of(new Opcao(1L,"Ferragens")));when(repo.fornecedores()).thenReturn(List.of(new Opcao(2L,"Fornecedor")));
  var opcoes=new ApoioServiceImp(repo).produto();assertEquals("Ferragens",opcoes.categorias().getFirst().nome());assertEquals("Fornecedor",opcoes.fornecedores().getFirst().nome());
 }
}
