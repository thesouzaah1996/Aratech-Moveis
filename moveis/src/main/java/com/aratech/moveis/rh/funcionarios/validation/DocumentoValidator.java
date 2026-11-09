package com.aratech.moveis.rh.funcionarios.validation;

import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.exception.CadastroFuncionarioException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

@Component
public class DocumentoValidator {

    public void validarDocumentos(FuncionarioDTO dto) {
        validarDocumentos(dto, true);
    }

    public void validarDocumentos(FuncionarioDTO dto, boolean exigirObrigatorios) {
        if (dto == null) {
            throw documentoInvalido("cadastro", "Informe os dados do funcionario.");
        }
        MultipartFile[] arquivos = {
            dto.getRgOuCnh(),
            dto.getCtps(),
            dto.getAso(),
            dto.getComprovanteResidencia(),
            dto.getTituloEleitor(),
            dto.getReservista(),
            dto.getDiplomaCertificado(),
            dto.getCertidaoCasamento(),
            dto.getCertidaoNascimento()
        };
        String[] campos = {
            "RG ou CNH",
            "CTPS",
            "ASO",
            "comprovante de residência",
            "título de eleitor",
            "reservista",
            "diploma ou certificado",
            "certidão de casamento",
            "certidão de nascimento"
        };
        long tamanhoTotal = 0;
        for (int indice = 0; indice < arquivos.length; indice++) {
            MultipartFile arquivo = arquivos[indice];
            if (exigirObrigatorios && indice < 4 && (arquivo == null || arquivo.isEmpty())) {
                throw documentoInvalido(campos[indice], "Envie o documento obrigatório.");
            }
            if (arquivo == null
                    || (arquivo.isEmpty()
                            && (arquivo.getOriginalFilename() == null
                                    || arquivo.getOriginalFilename().isBlank()))) {
                continue;
            }
            validarDocumento(arquivo, campos[indice]);
            tamanhoTotal += arquivo.getSize();
            if (tamanhoTotal > 20L * 1024 * 1024) {
                throw new CadastroFuncionarioException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "Os documentos do cadastro ultrapassam o limite de 20 MB.");
            }
        }
    }

    public void validarDocumento(MultipartFile arquivo, String campo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw documentoInvalido(campo, "O arquivo está vazio.");
        }
        if (arquivo.getSize() < 0 || arquivo.getSize() > 5L * 1024 * 1024) {
            throw new CadastroFuncionarioException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "O documento " + campo + " ultrapassa o limite de 5 MB.");
        }
        String nome = arquivo.getOriginalFilename();
        if (nome == null || !nome.matches("[A-Za-z0-9][A-Za-z0-9 _-]{0,199}\\.[A-Za-z0-9]{1,10}")) {
            throw documentoInvalido(
                    campo,
                    "O nome deve conter apenas letras, números, espaços, hífen ou sublinhado e uma"
                            + " extensão.");
        }
        String base = nome.substring(0, nome.indexOf('.'));
        if (base.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]) *")) {
            throw documentoInvalido(campo, "O nome do arquivo é reservado.");
        }
        String extensao = nome.substring(nome.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String tipoEsperado =
                switch (extensao) {
                    case "pdf" -> "application/pdf";
                    case "png" -> "image/png";
                    case "jpg", "jpeg" -> "image/jpeg";
                    default -> throw documentoInvalido(campo, "Envie um arquivo PDF, JPEG ou PNG.");
                };
        try (var stream = arquivo.getInputStream()) {
            byte[] conteudo = stream.readNBytes(5 * 1024 * 1024 + 1);
            if (conteudo.length > 5 * 1024 * 1024) {
                throw new CadastroFuncionarioException(
                        HttpStatus.PAYLOAD_TOO_LARGE, "O documento ultrapassa o limite de 5 MB.");
            }
            if (conteudo.length != arquivo.getSize()) {
                throw documentoInvalido(campo, "O tamanho informado não corresponde ao arquivo.");
            }
            byte[] prefixo = Arrays.copyOf(conteudo, Math.min(12, conteudo.length));
            byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            boolean assinaturaValida =
                    switch (extensao) {
                        case "pdf" ->
                                prefixo.length >= 5
                                        && prefixo[0] == '%'
                                        && prefixo[1] == 'P'
                                        && prefixo[2] == 'D'
                                        && prefixo[3] == 'F'
                                        && prefixo[4] == '-';
                        case "png" ->
                                prefixo.length >= png.length
                                        && Arrays.equals(Arrays.copyOf(prefixo, png.length), png);
                        default ->
                                prefixo.length >= 3
                                        && (prefixo[0] & 0xFF) == 0xFF
                                        && (prefixo[1] & 0xFF) == 0xD8
                                        && (prefixo[2] & 0xFF) == 0xFF;
                    };
            if (!assinaturaValida) {
                throw documentoInvalido(campo, "O conteúdo não corresponde ao formato informado.");
            }
            if (!extensao.equals("pdf")) {
                try (var imagemStream =
                        new javax.imageio.stream.MemoryCacheImageInputStream(
                                new java.io.ByteArrayInputStream(conteudo))) {
                    var leitores = javax.imageio.ImageIO.getImageReaders(imagemStream);
                    if (!leitores.hasNext()) {
                        throw documentoInvalido(campo, "Imagem inválida.");
                    }
                    var leitor = leitores.next();
                    try {
                        leitor.setInput(imagemStream, true, true);
                        int largura = leitor.getWidth(0);
                        int altura = leitor.getHeight(0);
                        if (largura <= 0
                                || altura <= 0
                                || largura > 10000
                                || altura > 10000
                                || (long) largura * altura > 20000000) {
                            throw documentoInvalido(
                                    campo, "A imagem ultrapassa o limite de 20 megapixels.");
                        }
                        if (leitor.read(0) == null) {
                            throw documentoInvalido(campo, "Imagem inválida.");
                        }
                    } finally {
                        leitor.dispose();
                    }
                }
            }
        } catch (IOException e) {
            throw documentoInvalido(campo, "Não foi possível ler o arquivo.");
        }
        if (arquivo.getContentType() != null && !arquivo.getContentType().isBlank()) {
            try {
                MediaType tipo = MediaType.parseMediaType(arquivo.getContentType());
                String informado = tipo.getType() + "/" + tipo.getSubtype();
                if (!informado.equalsIgnoreCase(tipoEsperado)
                        && !informado.equalsIgnoreCase("application/octet-stream")) {
                    throw documentoInvalido(campo, "O tipo informado não corresponde ao arquivo.");
                }
            } catch (IllegalArgumentException e) {
                throw documentoInvalido(campo, "O tipo do arquivo é inválido.");
            }
        }
    }

    private CadastroFuncionarioException documentoInvalido(String campo, String mensagem) {
        return new CadastroFuncionarioException(
                HttpStatus.BAD_REQUEST, "Documento " + campo + ": " + mensagem);
    }
}
