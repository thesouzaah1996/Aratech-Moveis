package com.aratech.moveis.rh.funcionarios.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.aratech.moveis.bucket.model.BucketFile;
import com.aratech.moveis.bucket.service.BucketService;
import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.exception.CadastroFuncionarioException;
import com.aratech.moveis.rh.funcionarios.service.imp.FuncionarioDocumentoServiceImp;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.Set;

class FuncionarioDocumentoServiceTest {

    private final BucketService bucket = mock(BucketService.class);
    private final FuncionarioDocumentoService service = new FuncionarioDocumentoServiceImp(bucket);

    private FuncionarioDTO dto() {
        var file =
                new MockMultipartFile(
                        "file", "documento.pdf", "application/pdf", "%PDF-1.7\n%%EOF".getBytes());
        return FuncionarioDTO.builder()
                .rgOuCnh(file)
                .ctps(file)
                .aso(file)
                .comprovanteResidencia(file)
                .build();
    }

    @Test
    void enviaObrigatoriosComChavesDiferentesPorCadastro() {
        var primeiro = service.upload(dto());
        var segundo = service.upload(dto());
        assertEquals(4, primeiro.size());
        assertNotEquals(primeiro.get("ctps"), segundo.get("ctps"));
        var captor = ArgumentCaptor.forClass(BucketFile.class);
        verify(bucket, times(8)).upload(captor.capture());
        assertEquals("application/pdf", captor.getValue().type().toString());
        assertEquals("%PDF-1.7\n%%EOF".getBytes().length, captor.getValue().size());
    }

    @Test
    void limpaUploadsParciaisQuandoMinioFalha() {
        doNothing().doThrow(new IllegalStateException("offline")).when(bucket).upload(any());
        var erro = assertThrows(CadastroFuncionarioException.class, () -> service.upload(dto()));
        assertEquals(502, erro.getStatus().value());
        verify(bucket, times(2)).remove(any());
    }

    @Test
    void limpaDocumentosQuandoTransacaoSofreRollback() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.upload(dto());
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(
                            sync ->
                                    sync.afterCompletion(
                                            TransactionSynchronization.STATUS_ROLLED_BACK));
            verify(bucket, times(4)).remove(any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void mantemDocumentosQuandoTransacaoConfirma() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.upload(dto());
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(
                            sync ->
                                    sync.afterCompletion(
                                            TransactionSynchronization.STATUS_COMMITTED));
            verify(bucket, never()).remove(any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void rejeitaNomesPerigososAntesDeEnviar() {
        for (String nome :
                new String[] {
                    "shell.php.jpg",
                    "../foto.png",
                    "foto;.pdf",
                    "CON.pdf",
                    "foto%00.pdf",
                    "foto.pdf ",
                    "foto:abc.pdf"
                }) {
            var dados = dto();
            dados.setCtps(
                    new MockMultipartFile("ctps", nome, "application/pdf", "%PDF-1.7".getBytes()));
            assertThrows(CadastroFuncionarioException.class, () -> service.upload(dados));
        }
        verifyNoInteractions(bucket);
    }

    @Test
    void rejeitaImagemComApenasAssinatura() {
        var dados = dto();
        dados.setCtps(
                new MockMultipartFile(
                        "ctps",
                        "foto.jpg",
                        "image/jpeg",
                        new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0}));
        assertThrows(CadastroFuncionarioException.class, () -> service.upload(dados));
        verifyNoInteractions(bucket);
    }

    @Test
    void reprocessaImagemERemoveConteudoAnexado() throws Exception {
        var imagem =
                new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(imagem, "png", bytes);
        byte[] original = bytes.toByteArray();
        bytes.write(
                "<script>payload</script>".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        var dados = dto();
        dados.setCtps(
                new MockMultipartFile(
                        "ctps", "foto.png", "application/octet-stream", bytes.toByteArray()));
        doAnswer(
                        invocation -> {
                            BucketFile arquivo = invocation.getArgument(0);
                            if (arquivo.type()
                                    .equals(org.springframework.http.MediaType.IMAGE_PNG)) {
                                assertArrayEquals(original, arquivo.inputStream().readAllBytes());
                                assertTrue(
                                        arquivo.name()
                                                .matches(
                                                        "funcionarios/[a-f0-9-]{36}/[a-f0-9-]{36}"));
                            }
                            return null;
                        })
                .when(bucket)
                .upload(any());
        service.upload(dados);
        verify(bucket, times(4)).upload(any());
    }

    @Test
    void soRemoveDocumentosSubstituidosDepoisDoCommit() {
        var antigos = Map.of("ctps", "old/ctps");
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.removerAposConfirmacao(antigos);
            verifyNoInteractions(bucket);
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
            verify(bucket).remove("old/ctps");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void naoRemoveDocumentosAnterioresQuandoAtualizacaoSofreRollback() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.removerAposConfirmacao(Map.of("ctps", "old/ctps"));
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(
                            sync ->
                                    sync.afterCompletion(
                                            TransactionSynchronization.STATUS_ROLLED_BACK));
            verifyNoInteractions(bucket);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void uploadDeAtualizacaoAceitaSomenteUmDocumentoEMantemCompensacao() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            var dados = FuncionarioDTO.builder().ctps(dto().getCtps()).build();
            var novos = service.uploadAtualizacao(dados);
            assertEquals(Set.of("ctps"), novos.keySet());
            verify(bucket).upload(any());
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(
                            sync ->
                                    sync.afterCompletion(
                                            TransactionSynchronization.STATUS_ROLLED_BACK));
            verify(bucket).remove(novos.get("ctps"));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
