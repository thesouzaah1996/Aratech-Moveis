package com.aratech.moveis.bucket;

import com.aratech.moveis.bucket.repository.BucketRepository;
import com.aratech.moveis.bucket.service.imp.BucketServiceImp;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BucketServiceImpTest {
 @Test void erroDeArmazenamentoNaoRetornaUrlFalsa(){
  var repo=mock(BucketRepository.class);var falha=new IllegalStateException("Armazenamento indisponível");when(repo.getUrl("documento.pdf")).thenThrow(falha);
  assertSame(falha,assertThrows(IllegalStateException.class,()->new BucketServiceImp(repo).getUrl("documento.pdf")));
 }
}
