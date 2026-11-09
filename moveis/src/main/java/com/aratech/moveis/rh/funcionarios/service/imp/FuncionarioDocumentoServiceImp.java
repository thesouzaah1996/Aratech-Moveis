package com.aratech.moveis.rh.funcionarios.service.imp;

import com.aratech.moveis.bucket.model.BucketFile;
import com.aratech.moveis.bucket.service.BucketService;
import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.exception.CadastroFuncionarioException;
import com.aratech.moveis.rh.funcionarios.service.FuncionarioDocumentoService;
import com.aratech.moveis.rh.funcionarios.validation.DocumentoValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FuncionarioDocumentoServiceImp implements FuncionarioDocumentoService {

    private final BucketService bucketService;

    public Map<String, String> upload(FuncionarioDTO dto) {
        return upload(dto, true);
    }

    public Map<String, String> uploadAtualizacao(FuncionarioDTO dto) {
        return upload(dto, false);
    }

    private Map<String, String> upload(FuncionarioDTO dto, boolean exigirObrigatorios) {
        // Validar também quando o upload for chamado sem passar pelo serviço de cadastro.
        new DocumentoValidator().validarDocumentos(dto, exigirObrigatorios);
        Map<String, String> documentos = new LinkedHashMap<>();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            if (status == STATUS_ROLLED_BACK) {
                                remover(documentos);
                            }
                        }
                    });
        }

        String pasta = "funcionarios/" + UUID.randomUUID() + "/";
        try {
            enviar(documentos, pasta, "rgOuCnh", dto.getRgOuCnh());
            enviar(documentos, pasta, "ctps", dto.getCtps());
            enviar(documentos, pasta, "aso", dto.getAso());
            enviar(documentos, pasta, "comprovanteResidencia", dto.getComprovanteResidencia());
            enviar(documentos, pasta, "tituloEleitor", dto.getTituloEleitor());
            enviar(documentos, pasta, "reservista", dto.getReservista());
            enviar(documentos, pasta, "diplomaCertificado", dto.getDiplomaCertificado());
            enviar(documentos, pasta, "certidaoCasamento", dto.getCertidaoCasamento());
            enviar(documentos, pasta, "certidaoNascimento", dto.getCertidaoNascimento());
            return documentos;
        } catch (IOException | RuntimeException e) {
            remover(documentos);
            documentos.clear();
            log.error("Falha ao enviar documentos do cadastro de funcionário.", e);
            throw new CadastroFuncionarioException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível salvar os documentos. Tente novamente.");
        }
    }

    public void removerAposConfirmacao(Map<String, String> documentos) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            remover(documentos);
                        }
                    });
        } else {
            remover(documentos);
        }
    }

    public void remover(Map<String, String> documentos) {
        documentos
                .values()
                .forEach(
                        objeto -> {
                            try {
                                bucketService.remove(objeto);
                            } catch (RuntimeException e) {
                                log.error(
                                        "Falha ao remover documento após cancelamento do cadastro:"
                                                + " {}",
                                        objeto,
                                        e);
                            }
                        });
    }

    private void enviar(
            Map<String, String> documentos, String pasta, String tipo, MultipartFile arquivo)
            throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            return;
        }

        // O nome recebido nunca compõe a chave; objetos ficam no MinIO, fora da árvore web.
        String objeto = pasta + UUID.randomUUID();
        String extensao =
                arquivo.getOriginalFilename()
                        .substring(arquivo.getOriginalFilename().lastIndexOf('.') + 1)
                        .toLowerCase(Locale.ROOT);
        MediaType contentType =
                switch (extensao) {
                    case "pdf" -> MediaType.APPLICATION_PDF;
                    case "png" -> MediaType.IMAGE_PNG;
                    default -> MediaType.IMAGE_JPEG;
                };
        byte[] conteudo;
        try (var stream = arquivo.getInputStream()) {
            conteudo = stream.readNBytes(5 * 1024 * 1024 + 1);
        }
        if (conteudo.length > 5 * 1024 * 1024 || conteudo.length != arquivo.getSize()) {
            throw new IOException("Tamanho de documento inválido.");
        }
        if (!extensao.equals("pdf")) {
            // Recodificar só os pixels elimina metadados e conteúdo anexado ao arquivo original.
            var imagem = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(conteudo));
            if (imagem == null) {
                throw new IOException("Imagem inválida.");
            }
            var saida = new java.io.ByteArrayOutputStream();
            if (!javax.imageio.ImageIO.write(
                    imagem, extensao.equals("png") ? "png" : "jpeg", saida)) {
                throw new IOException("Não foi possível reprocessar a imagem.");
            }
            conteudo = saida.toByteArray();
            if (conteudo.length > 5 * 1024 * 1024) {
                throw new IOException("Imagem reprocessada ultrapassa o limite de 5 MB.");
            }
        }
        documentos.put(tipo, objeto);
        try (var stream = new java.io.ByteArrayInputStream(conteudo)) {
            bucketService.upload(new BucketFile(objeto, stream, contentType, conteudo.length));
        }
    }
}
