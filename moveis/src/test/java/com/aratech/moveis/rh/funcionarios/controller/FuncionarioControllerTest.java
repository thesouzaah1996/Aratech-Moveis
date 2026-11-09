package com.aratech.moveis.rh.funcionarios.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.dto.FuncionarioResumoDTO;
import com.aratech.moveis.rh.funcionarios.dto.Response;
import com.aratech.moveis.rh.funcionarios.exception.CadastroFuncionarioException;
import com.aratech.moveis.rh.funcionarios.service.FuncionarioService;
import com.aratech.moveis.security.service.GestaoAcessoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class FuncionarioControllerTest {

    private final FuncionarioService service = mock(FuncionarioService.class);
    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc =
                MockMvcBuilders.standaloneSetup(
                                new FuncionarioController(service, mock(GestaoAcessoService.class)))
                        .setControllerAdvice(new FuncionarioExceptionHandler())
                        .build();
    }

    @Test
    void recebeFormularioComDocumentosERetorna201() throws Exception {
        when(service.addFuncionario(any()))
                .thenReturn(
                        Response.builder()
                                .status(201)
                                .mensagem("Funcionário cadastrado com sucesso.")
                                .funcionarioId(10L)
                                .nomeCompleto("Ana Silva")
                                .ativo(true)
                                .build());

        mvc.perform(formulario())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.funcionarioId").value(10))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.token").doesNotExist());

        var captor = ArgumentCaptor.forClass(FuncionarioDTO.class);
        verify(service).addFuncionario(captor.capture());
        assertEquals("Ana Silva", captor.getValue().getNomeCompleto());
        assertEquals("52998224725", captor.getValue().getCpf());
        assertEquals("1990-01-01", captor.getValue().getDataNascimento().toString());
        assertEquals("documento.pdf", captor.getValue().getRgOuCnh().getOriginalFilename());
        assertFalse(captor.getValue().getCtps().isEmpty());
    }

    @Test
    void retorna409ComMensagemQuandoCpfJaExiste() throws Exception {
        when(service.addFuncionario(any()))
                .thenThrow(
                        new CadastroFuncionarioException(
                                HttpStatus.CONFLICT, "Já existe um funcionário com este CPF."));

        mvc.perform(formulario())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("Já existe um funcionário com este CPF."))
                .andExpect(jsonPath("$.funcionarioId").doesNotExist());
    }

    @Test
    void retorna502QuandoArmazenamentoDeDocumentosFalha() throws Exception {
        when(service.addFuncionario(any()))
                .thenThrow(
                        new CadastroFuncionarioException(
                                HttpStatus.BAD_GATEWAY,
                                "Não foi possível salvar os documentos. Tente novamente."));

        mvc.perform(formulario())
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(
                        jsonPath("$.mensagem")
                                .value("Não foi possível salvar os documentos. Tente novamente."));
    }

    @Test
    void retornaErrosDeValidacaoInformadosPelaService() throws Exception {
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var erros = factory.getValidator().validate(new FuncionarioDTO());
            when(service.addFuncionario(any()))
                    .thenThrow(new jakarta.validation.ConstraintViolationException(erros));
        }
        mvc.perform(multipart("/aratech/rh/funcionarios"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.cpf").exists());
        verify(service).addFuncionario(any());
    }

    @Test
    void rejeitaDataInvalidaComRespostaPadronizada() throws Exception {
        var request = formulario();
        request.with(
                servletRequest -> {
                    servletRequest.setParameter("dataNascimento", "data-invalida");
                    return servletRequest;
                });
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.dataNascimento").exists());
        verifyNoInteractions(service);
    }

    @Test
    void naoPermiteDefinirCamposInternosPeloFormulario() throws Exception {
        when(service.addFuncionario(any()))
                .thenReturn(Response.builder().status(201).ativo(true).build());
        mvc.perform(
                        formulario()
                                .param("id", "999")
                                .param("ativo", "false")
                                .param("documentos[ctps]", "objeto-de-outra-pessoa"))
                .andExpect(status().isCreated());
        verify(service).addFuncionario(any(FuncionarioDTO.class));
    }

    private MockMultipartHttpServletRequestBuilder formulario() {
        var request = multipart("/aratech/rh/funcionarios");
        for (String campo : new String[] {"rgOuCnh", "ctps", "aso", "comprovanteResidencia"}) {
            request.file(
                    new MockMultipartFile(
                            campo, "documento.pdf", "application/pdf", new byte[] {1}));
        }
        request.param("nomeCompleto", "Ana Silva");
        request.param("cpf", "52998224725");
        request.param("dataNascimento", "1990-01-01");
        request.param("sexo", "FEMININO");
        request.param("nomeMae", "Maria Silva");
        request.param("cep", "12345678");
        request.param("logradouro", "Rua das Flores");
        request.param("numero", "10");
        request.param("bairro", "Centro");
        request.param("cidade", "São Paulo");
        request.param("uf", "SP");
        request.param("dataAdmissao", "2026-01-01");
        request.param("tipoContrato", "CLT");
        request.param("cargo", "Analista");
        request.param("tipoFuncionario", "ADMINISTRATIVO");
        request.param("setor", "RH");
        request.param("salario", "3000.00");
        request.param("email", "ana@exemplo.com");
        request.param("banco", "Banco Exemplo");
        request.param("agencia", "1234");
        request.param("conta", "123456-7");
        request.param("tipoConta", "CORRENTE");
        return request;
    }

    @Test
    void consultaPaginadaNaoExpoeDadosBancariosCpfOuChavesDeDocumentos() throws Exception {
        var resumo =
                new FuncionarioResumoDTO(
                        42L, "Ana Silva", "ana@example.com", "Analista", "RH", null, true);
        when(service.getAllFuncionarios(0, 20))
                .thenReturn(
                        Response.builder()
                                .status(200)
                                .funcionarios(List.of(resumo))
                                .pagina(0)
                                .tamanho(20)
                                .total(1L)
                                .build());
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                "/aratech/rh/funcionarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.funcionarios[0].id").value(42))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.funcionarios[0].cpf").doesNotExist())
                .andExpect(jsonPath("$.funcionarios[0].salario").doesNotExist())
                .andExpect(jsonPath("$.funcionarios[0].conta").doesNotExist())
                .andExpect(jsonPath("$.funcionarios[0].documentos").doesNotExist());
    }

    @Test
    void consultaInexistenteRetorna404Padronizado() throws Exception {
        when(service.getFuncionarioById(42L))
                .thenThrow(
                        new CadastroFuncionarioException(
                                HttpStatus.NOT_FOUND, "Funcionario nao encontrado."));
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                "/aratech/rh/funcionarios/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void atualizacaoMultipartRecebeDadosSemPermitirAlterarStatusOuReferencias() throws Exception {
        when(service.updateFuncionario(any(), eq(42L)))
                .thenReturn(Response.builder().status(200).ativo(false).build());
        var request = formulario();
        request.with(
                servletRequest -> {
                    servletRequest.setMethod("PUT");
                    servletRequest.setRequestURI("/aratech/rh/funcionarios/42");
                    return servletRequest;
                });
        mvc.perform(request.param("ativo", "true").param("documentos[ctps]", "outro/ctps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
        verify(service).updateFuncionario(any(FuncionarioDTO.class), eq(42L));
    }

    @Test
    void endpointsDeStatusUsamPatchSemDadosCadastrais() throws Exception {
        when(service.enableEmployee(42L))
                .thenReturn(Response.builder().status(200).ativo(true).build());
        when(service.disableEmployee(42L))
                .thenReturn(Response.builder().status(200).ativo(false).build());
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                "/aratech/rh/funcionarios/42/ativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                "/aratech/rh/funcionarios/42/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }
}
