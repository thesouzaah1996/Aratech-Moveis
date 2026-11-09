package com.aratech.moveis.rh.funcionarios;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.enums.Sexo;
import com.aratech.moveis.rh.funcionarios.enums.TipoConta;
import com.aratech.moveis.rh.funcionarios.enums.TipoContrato;
import com.aratech.moveis.rh.funcionarios.enums.TipoFuncionario;
import com.aratech.moveis.rh.funcionarios.enums.UF;
import com.aratech.moveis.rh.funcionarios.exception.CadastroFuncionarioException;
import com.aratech.moveis.rh.funcionarios.repository.FuncionarioRepository;
import com.aratech.moveis.rh.funcionarios.service.FuncionarioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@Transactional
class FuncionarioCadastroIT {
    @Autowired FuncionarioService service;
    @Autowired FuncionarioRepository repository;

    @Test
    void cadastraComDocumentosPersisteEConsultaDepois() throws Exception {
        String marca = UUID.randomUUID().toString();
        String cpf = String.valueOf(new Random().nextInt(100000000, 999999999));
        for (int pos = 9; pos <= 10; pos++) {
            int soma = 0;
            for (int i = 0; i < pos; i++) soma += (cpf.charAt(i) - '0') * (pos + 1 - i);
            int resto = soma % 11;
            cpf += resto < 2 ? 0 : 11 - resto;
        }
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(
                new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB),
                "png",
                bytes);
        var file = new MockMultipartFile("file", "documento.png", "image/png", bytes.toByteArray());
        var d =
                FuncionarioDTO.builder()
                        .nomeCompleto("Funcionario Teste " + marca)
                        .cpf(cpf)
                        .email("teste-" + marca + "@example.invalid")
                        .dataNascimento(LocalDate.of(1990, 1, 1))
                        .sexo(Sexo.NAO_INFORMADO)
                        .nomeMae("Pessoa Ficticia")
                        .cep("01001000")
                        .logradouro("Rua de Teste")
                        .numero("10")
                        .bairro("Centro")
                        .cidade("Sao Paulo")
                        .uf(UF.SP)
                        .dataAdmissao(LocalDate.of(2026, 1, 1))
                        .tipoContrato(TipoContrato.CLT)
                        .cargo("Analista")
                        .tipoFuncionario(TipoFuncionario.ADMINISTRATIVO)
                        .setor("RH")
                        .salario(new BigDecimal("3000.25"))
                        .banco("Banco de Teste")
                        .agencia("1234")
                        .conta("123456-7")
                        .tipoConta(TipoConta.CORRENTE)
                        .rgOuCnh(file)
                        .ctps(file)
                        .aso(file)
                        .comprovanteResidencia(file)
                        .build();
        var resposta = service.addFuncionario(d);
        assertEquals(201, resposta.getStatus());
        assertTrue(resposta.getAtivo());
        var salvo = repository.findById(resposta.getFuncionarioId()).orElseThrow();
        assertEquals(4, salvo.getDocumentos().size());
        assertEquals(0, new BigDecimal("3000.25").compareTo(salvo.getSalario()));
        assertEquals(
                d.getNomeCompleto(),
                service.getFuncionarioById(salvo.getId()).getFuncionario().nomeCompleto());
        assertTrue(
                service.getAllFuncionarios(0, 100).getFuncionarios().stream()
                        .anyMatch(f -> f.id().equals(salvo.getId())));
        assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(d));
    }
}
