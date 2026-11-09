package com.aratech.moveis.bucket.controller;

import com.aratech.moveis.bucket.model.BucketFile;
import com.aratech.moveis.bucket.service.BucketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Tag(
        name = "Infraestrutura — Upload legado",
        description =
                "Endpoint legado /bucket mantido bloqueado pela política atual de segurança,"
                    + " inclusive para usuários autenticados. Esta documentação não libera seu uso."
                    + " Os documentos de funcionários devem ser enviados pelo multipart do RH. O"
                    + " contrato legado recebe file e retorna texto; sua existência não significa"
                    + " autorização de acesso.")
@RestController
@RequestMapping("/bucket")
@RequiredArgsConstructor
public class BucketController {

    private final BucketService service;

    @Operation(
            summary = "Consultar contrato de upload legado bloqueado",
            description =
                    "Contrato existente apenas para referência; a política de segurança não permite"
                            + " executá-lo. Não utilize em novas integrações.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            MediaType type = MediaType.parseMediaType(file.getContentType());
            var bucketFile =
                    new BucketFile(file.getOriginalFilename(), inputStream, type, file.getSize());
            service.upload(bucketFile);
            return ResponseEntity.ok("Arquivo enviado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao enviar o arquivo: " + e.getMessage());
        }
    }
}
